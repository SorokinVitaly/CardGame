package com.example.cardgame

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val localData: LocalDataRepository,
    private val history: History
) : ViewModel() {
    private val savedState = loadSavedState()

    private val _state = MutableStateFlow(savedState.screenState)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val deck = savedState.deck.toMutableList()
    private val discarded = savedState.discarded.toMutableList()
    private var currentBet = savedState.currentBet
    private var numOfRaise = savedState.numOfRaise
    private var playerIndex = savedState.playerIndex
    private var round = savedState.round
    private val combinations = arrayOfNulls<DrawCombination?>(PLAYERS_NUMBER)

    init {
        if (localData.isGameStarted &&
            state.value.players.all { it.cards.size == 5 || !it.isActive }
        ) {
            viewModelScope.launch {
                _state.update { it.copy(isActionAvailable = false) }
                if (round == RoundType.PRE_DRAW) {
                    preCalculatePreDraw()
                } else {
                    preCalculatePostDraw()
                }
                mainGameLoop()
            }
        }
    }

    fun onResetGame() {
        viewModelScope.launch {
            localData.resetGame()
            _state.update { loadSavedState().screenState }
            val mess = "Game was restarted"
            delay(500L)
            logAndShow(mess)
        }
    }

    fun onDealNext() {
        viewModelScope.launch {
            localData.isResetAvailable = true
            localData.isGameStarted = true
            currentBet = 0
            numOfRaise = 0
            playerIndex = localData.dealerIndex
            round = RoundType.PRE_DRAW
            history.clear()
            discarded.clear()
            if (deck.size != 52) {
                deck.clear()
                deck.addAll(deckPoker)
                deck.shuffle()
            }
            initialState()
            saveState()
            dealingCards()
            preCalculatePreDraw()
            payBlinds()
            mainGameLoop()
        }
    }

    fun onAction(action: ActionType) {
        viewModelScope.launch {
            _state.update { it.copy(isActionAvailable = false, isDrawEnabled = false) }
            applyAction(0, action)
            saveState()
            mainGameLoop()
        }
    }

    fun onCardClick(card: Card) {
        _state.update {
            it.updatePlayer(0) {
                copy(
                    selectedCards = if (card in selectedCards) {
                        selectedCards - card
                    } else {
                        selectedCards + card
                    }
                )
            }
        }
    }

    private fun initialState() {
        _state.update {
            it.copy(
                actionsAvailable = emptyList(),
                bankChips = 0,
                isActionAvailable = false,
                isDealAvailable = true,
                isResetAvailable = true,
                isCardsOpen = false,
                players = it.players.mapIndexed { i, player ->
                    player.copy(
                        cards = emptyList(),
                        lastDraw = ActionType.NoAction(),
                        lastBet = ActionType.NoAction(),
                        isDealer = i == localData.dealerIndex
                    )
                }
            )
        }
    }

    private suspend fun forEachActivePlayer(action: suspend PlayerData.(Int) -> Unit) {
        repeat(PLAYERS_NUMBER) { index ->
            player(index).apply {
                if (isActive) {
                    action(index)
                }
            }
        }
    }

    private suspend fun dealingCards() {
        _state.update { it.updateAllPlayers { clearCards() } }
        repeat(5) {
            forEachActivePlayer { index ->
                delay(300L)
                val card = deck.removeAt(deck.lastIndex)
                _state.update { it.updatePlayer(index) { addCard(card) } }
            }
        }
        delay(500L)
        _state.update { it.updateAllPlayers { sortCards() } }
    }

    private suspend fun preCalculatePreDraw() {
        forEachActivePlayer { index ->
            combinations[index] = calcPreDrawCombination(history, cards)
        }
    }

    private suspend fun preCalculatePostDraw() {
        forEachActivePlayer { index ->
            if (lastBet !is ActionType.Fold) {
                combinations[index] = DrawCombination(calcCombination(cards))
            }
        }
    }

    private suspend fun payBlinds() {
        delay(300L)
        playerIndex = nextPlayerIndex(playerIndex) { isActive }
        applyAction(playerIndex, ActionType.SmallBlind())
        delay(300L)
        playerIndex = nextPlayerIndex(playerIndex) { isActive }
        applyAction(playerIndex, ActionType.BigBlind())
    }

    private suspend fun mainGameLoop() {
        while (true) {
            val inGamePlayers = _state.value.players.filter { it.isInGame }
            if (inGamePlayers.size == 1) {
                takeBank(listOf(_state.value.players.indexOfFirst { it.isInGame }))
                gameOver()
                return
            }

            val endRound = if (round != RoundType.DRAW) {
                inGamePlayers.all {
                    it.lastBet.paid == currentBet &&
                            it.lastBet !is ActionType.NoAction &&
                            it.lastBet !is ActionType.SmallBlind &&
                            it.lastBet !is ActionType.BigBlind
                }
            } else {
                inGamePlayers.all { it.lastDraw is ActionType.Draw }
            }
            if (endRound) {
                if (endRound()) {
                    return
                }
            }

            playerIndex = nextPlayerIndex(playerIndex) { isInGame }
            val availableActions = availableActions(playerIndex)
            if (playerIndex == 0) {
                if (round == RoundType.DRAW) {
                    _state.update { it.copy(isDrawEnabled = true) }
                    _events.emit(UiEvent.ShowToast("Please choose cards to draw"))
                }
                _state.update { it.copy(isActionAvailable = true, actionsAvailable = availableActions) }
                return
            } else {
                val action = botBetting(playerIndex, availableActions)
                applyAction(playerIndex, action)
            }
        }
    }

    private suspend fun endRound(): Boolean {
        val newRound = when (round) {
            RoundType.PRE_DRAW -> {
                RoundType.DRAW
            }
            RoundType.DRAW -> {
                endDrawRound()
                preCalculatePostDraw()
                RoundType.POST_DRAW
            }
            RoundType.POST_DRAW -> {
                endPostDrawRound()
                return true
            }
        }
        playerIndex = localData.dealerIndex
        round = newRound
        numOfRaise = 0
        currentBet = 0
        return false
    }

    private fun endDrawRound() {
        _state.update {
            it.copy(
                players = it.players.map { player ->
                    if (player.isInGame) {
                        player.copy(lastBet = ActionType.NoAction())
                    } else {
                        player
                    }
                }
            )
        }
    }

    private suspend fun endPostDrawRound() {
        _state.update { it.copy(isCardsOpen = true) }
        val inGameCombinations = _state.value.players.mapIndexedNotNull { i, playerData ->
            if (playerData.isInGame) {
                val combination = combinations[i]
                requireNotNull(combination)
                i to combination.onHandCombination
            }
            else {
                null
            }
        }
        val winCombination = inGameCombinations.maxBy { it.second }.second
        val winIndexes = inGameCombinations.filter { it.second == winCombination }.map { it.first }
        takeBank(winIndexes)
        gameOver()
    }

    private suspend fun takeBank(winIndexes: List<Int>) {
        require(winIndexes.isNotEmpty())
        val winnersNames = winIndexes.joinToString { player(it).name }
        val mess = "$winnersNames won and take bank ${_state.value.bankChips} chips"
        logAndShow(mess)

        fun take(index: Int, amount: Int) {
            _state.update { it.takeFromBank(index, amount) }
        }

        val numWinners = winIndexes.size
        val part = _state.value.bankChips / numWinners
        if (part > 0) {
            winIndexes.forEach { index ->
                take(index, part)
            }
        }

        val winIndexesFirst = winIndexes.filter { it > localData.dealerIndex }
        winIndexesFirst.forEach { index ->
            if (_state.value.bankChips > 0) {
                take(index, 1)
            }
        }

        val winIndexesLast = winIndexes.filter { it <= localData.dealerIndex }
        winIndexesLast.forEach { index ->
            if (_state.value.bankChips > 0) {
                take(index, 1)
            }
        }
    }

    private fun gameOver() {
        val isPlayerActive = state.value.players.map { it.chips >= BIG_BLIND }
        val isDealAvailable = isPlayerActive[0] && isPlayerActive.count { it } > 1
        _state.update {
            it.copy(
                actionsAvailable = emptyList(),
                isDrawEnabled = false,
                isActionAvailable = true,
                isDealAvailable = isDealAvailable,
                isResetAvailable = true,
                players = it.players.mapIndexed { i, player ->
                    player.copy(isActive = isPlayerActive[i])
                }
            )
        }
        localData.dealerIndex = nextPlayerIndex(localData.dealerIndex) { isActive }
        localData.isGameStarted = false
        saveState()
    }

    private fun nextPlayerIndex(index: Int, predicate: PlayerData.() -> Boolean): Int {
        val first = (index + 1) % PLAYERS_NUMBER
        var current = first

        while (true) {
            if (player(current).predicate()) {
                return current
            }
            current = (current + 1) % PLAYERS_NUMBER
            if (current == first) {
                throw IllegalStateException("Next player not found")
            }
        }
    }

    private fun availableActions(playerIndex: Int): List<ActionType> {
        if (round == RoundType.DRAW) {
            return listOf(ActionType.Draw())
        }
        val betSize = if (round == RoundType.PRE_DRAW) SMALL_BET else BIG_BET
        val player = player(playerIndex)
        val chips = player.chips
        val prevPaid = player.lastBet.paid
        val payToCall = currentBet - prevPaid
        val bets = ArrayList<ActionType>()

        if (payToCall == 0) {
            bets.add(ActionType.Check(currentBet))
        } else {
            if (chips >= payToCall) {
                bets.add(ActionType.Call(currentBet, prevPaid))
            }
        }

        if (currentBet == 0) {
            if (chips >= betSize) {
                bets.add(ActionType.Bet(betSize))
            }
        } else {
            val raiseTo = currentBet + betSize
            if (chips >= raiseTo - prevPaid && numOfRaise < MAX_NUM_OF_RAISE) {
                bets.add(ActionType.Raise(raiseTo, prevPaid))
            }
        }

        bets.add(ActionType.Fold())
        return bets
    }

    private fun botBetting(index: Int, availableActions: List<ActionType>): ActionType {
        val firstAction = availableActions.first()
        return if (firstAction is ActionType.Draw) {
            val combination = combinations[index]
            requireNotNull(combination)
            _state.update { it.updatePlayer(index) { setSelected(combination.cardsForDraw) } }
            firstAction
        } else {
            val isPreDraw = round == RoundType.PRE_DRAW
            val combination = combinations[index]
            requireNotNull(combination)
            val strength = calcHandStrength(combination, isPreDraw)
            val playerCount = _state.value.players.count { it.isActive }
            val positionFromDealer = (index - localData.dealerIndex - 1 + playerCount) % playerCount
            val isLatePosition = positionFromDealer >= 2
            val payToCall = currentBet - player(index).lastBet.paid
            val strategy = selectBotStrategy(
                strength,
                payToCall,
                numOfRaise,
                isLatePosition,
                isPreDraw,
                combination.incompleteCombination
            )
            resolveAction(strategy, availableActions)
        }
    }

    private suspend fun applyAction(index: Int, action: ActionType) {
        log("$index: ${action.name}")
        if (action is ActionType.Raise) {
            numOfRaise++
        }
        if (action is ActionType.Draw) {
            applyDrawAction(index)
        } else {
            if (action !is ActionType.Fold) {
                currentBet = action.paid
                val amountToPay = action.payNow
                if (amountToPay > 0) {
                    _state.update { it.payToBank(index, amountToPay) }
                }
            }
            history.add(index, action)
            _state.update { it.updatePlayer(index) { copy(lastBet = action) } }
        }
    }

    private suspend fun applyDrawAction(index: Int) {
        val selected = _state.value.players[index].selectedCards
        val newAction = ActionType.Draw(selected.size)
        if (selected.isNotEmpty()) {
            if (deck.size < selected.size) {
                deck.addAll(discarded)
                discarded.clear()
                deck.shuffle()
            }
            discarded.addAll(selected)
            selected.forEach { card ->
                _state.update { it.updatePlayer(index) { removeCard(card) } }
                delay(300L)
            }
            delay(500L)
            selected.forEach { card ->
                val newCard = deck.removeAt(deck.lastIndex)
                _state.update { it.updatePlayer(index) { addCard(newCard) } }
                delay(300L)
            }
            _state.update { it.updatePlayer(index) { clearSelected().sortCards() } }
        }
        history.add(index, newAction)
        _state.update { it.updatePlayer(index) { copy(lastDraw = newAction) } }
    }

    private fun saveState() {
        val savedState = SavedState(
            state.value,
            currentBet,
            numOfRaise,
            playerIndex,
            round,
            deck,
            discarded
        )
        saveSnapshot(localData, history, savedState)
    }

    private fun loadSavedState(): SavedState =
        try {
            restoreSnapshot(localData, history)
        } catch(_: Exception) {
            log("Local data is broken. Game was restarted")
            localData.resetGame()
            restoreSnapshot(localData, history)
        }

    private fun player(index: Int) = _state.value.players[index]

    private fun log(mess: String) = Log.e("GamePlay", mess)

    private suspend fun logAndShow(mess: String) {
        log(mess)
        _events.emit(UiEvent.ShowToast(mess))
    }
}