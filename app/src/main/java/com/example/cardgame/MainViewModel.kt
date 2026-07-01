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
    private var currentBet = savedState.currentBet
    private var numOfRaise = savedState.numOfRaise
    private var playerIndex = savedState.playerIndex
    private var round = savedState.round
    private val combinations = arrayOfNulls<DrawCombination?>(4)

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

    fun onDialNext() {
        viewModelScope.launch {
            localData.isResetAvailable = true
            localData.isGameStarted = true
            currentBet = 0
            numOfRaise = 0
            playerIndex = localData.dealerIndex
            round = RoundType.PRE_DRAW
            history.clear()
            if (deck.size != 52) {
                newDeck()
            }
            initialState()
            saveSnapshot()
            dealingCards()
            preCalculatePreDraw()
            payAnte()
            mainGameLoop()
        }
    }

    fun onAction(action: ActionType) {
        viewModelScope.launch {
            _state.update { it.copy(isActionAvailable = false, isDrawEnabled = false) }
            applyAction(0, action)
            saveSnapshot()
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
                        isDialer = i == localData.dealerIndex
                    )
                }
            )
        }
    }

    private fun newDeck() {
        deck.clear()
        deck.addAll(deckPoker)
        deck.shuffle()
    }

    private suspend fun dealingCards() {
        _state.update { it.updateAllPlayers { clearCards() } }
        repeat(5) {
            repeat(4) { index ->
                if (player(index).isActive) {
                    delay(300L)
                    val card = deck.removeAt(deck.lastIndex)
                    _state.update { it.updatePlayer(index) { addCard(card) } }
                }
            }
        }
        delay(500L)
        _state.update { it.updateAllPlayers { sortCards() } }
    }

    private fun preCalculatePreDraw() {
        repeat(4) { index ->
            if (player(index).isActive) {
                combinations[index] = calcPreDrawCombination(history, player(index).cards)
            }
        }
    }

    private fun preCalculatePostDraw() {
        repeat(4) { index ->
            if (player(index).isInGame) {
                combinations[index] = DrawCombination(
                    onHandCombination = calcCombination(player(index).cards)
                )
            }
        }
    }

    private suspend fun payAnte() {
        repeat(4) { index ->
            if (player(index).isActive) {
                delay(300L)
                _state.update { it.payToBank(index, ANTE_BET) }
            }
        }
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
                inGamePlayers.all { it.lastBet !is ActionType.NoAction && it.lastBet.paid == currentBet }
            } else {
                inGamePlayers.all { it.lastDraw is ActionType.Draw }
            }
            if (endRound) {
                if (endRound()) {
                    return
                }
            }

            val prevPlayerIndex = playerIndex
            playerIndex = nextInGameIndex(playerIndex)
            log("$prevPlayerIndex -> $playerIndex")

            val availableActions = availableActions(playerIndex)
            if (playerIndex == 0) {
                if (round == RoundType.DRAW) {
                    log("player0 Draw")
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
        localData.dealerIndex = nextInGameIndex(localData.dealerIndex)
        localData.isGameStarted = false
        _state.update { it.copy(
            actionsAvailable = emptyList(),
            isDrawEnabled = false,
            isActionAvailable = true,
            isDealAvailable = true,
            isResetAvailable = true
        ) }
        saveSnapshot()
    }

    private fun nextInGameIndex(index: Int): Int {
        val first = (index + 1) and 3
        var current = first

        while (true) {
            if (player(current).isInGame) {
                return current
            }
            current = (current + 1) and 3
            if (current == first) {
                throw IllegalStateException("Next player not found")
            }
        }
    }

    private fun availableActions(playerIndex: Int): List<ActionType> {
        if (round == RoundType.DRAW) {
            return listOf(ActionType.Draw())
        }
        val chips = player(playerIndex).chips
        val roundBet = if (round == RoundType.PRE_DRAW) PRE_DRAW_BET else POST_DRAW_BET
        val bets = ArrayList<ActionType>()

        if (currentBet == 0) {
            bets.add(ActionType.Check())
            if (chips >= roundBet) {
                bets.add(ActionType.Bet(roundBet))
            }
        } else {
            val prevPaid = player(playerIndex).lastBet.paid
            if (chips >= currentBet - prevPaid) {
                bets.add(ActionType.Call(currentBet, prevPaid))
            }

            val raiseTo = currentBet + roundBet
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
            val isFacingBet = currentBet > 0

            val potOdds: Float = currentBet.toFloat() / (_state.value.bankChips + currentBet).toFloat()
            val drawProbability = if (isPreDraw) drawOdds[combination.incompleteCombination] else 0f
            requireNotNull(drawProbability)
            val drawIsWorthIt = drawProbability > 0f && potOdds <= drawProbability

            val strategy = selectBotStrategy(
                strength,
                numOfRaise,
                isLatePosition,
                drawIsWorthIt,
                isFacingBet,
                isPreDraw
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

    private fun saveSnapshot() {
        val players = state.value.players
        with (localData) {
            player0Name = players[0].name
            player0Cards = Card.serializeList(players[0].cards)
            player0Chips = players[0].chips
            player0IsActive = players[0].isActive
            player0LastDraw = players[0].lastDraw.serialize()
            player0LastBet = players[0].lastBet.serialize()

            player1Name = players[1].name
            player1Cards = Card.serializeList(players[1].cards)
            player1Chips = players[1].chips
            player1IsActive = players[1].isActive
            player1LastDraw = players[1].lastDraw.serialize()
            player1LastBet = players[1].lastBet.serialize()

            player2Name = players[2].name
            player2Cards = Card.serializeList(players[2].cards)
            player2Chips = players[2].chips
            player2IsActive = players[2].isActive
            player2LastDraw = players[2].lastDraw.serialize()
            player2LastBet = players[2].lastBet.serialize()

            player3Name = players[3].name
            player3Cards = Card.serializeList(players[3].cards)
            player3Chips = players[3].chips
            player3IsActive = players[3].isActive
            player3LastDraw = players[3].lastDraw.serialize()
            player3LastBet = players[3].lastBet.serialize()

            bankChips = state.value.bankChips
            isResetAvailable = state.value.isResetAvailable
        }
        localData.currentBet = currentBet
        localData.numOfRaise = numOfRaise
        localData.playerIndex = playerIndex
        localData.round = round.ordinal
        localData.deck = Card.serializeList(deck)
        localData.history = history.serialize()
    }

    private fun restoreSnapshot(): SavedState {
        history.unserialize(localData.history)

        val screenState = with (localData) {
            val player0 = PlayerData(
                name = player0Name,
                cards = Card.unserializeList(player0Cards),
                chips = player0Chips,
                isActive = player0IsActive,
                isDialer = dealerIndex == 0,
                lastDraw = ActionType.unserialize(player0LastDraw),
                lastBet = ActionType.unserialize(player0LastBet)
            )
            val player1 = PlayerData(
                name = player1Name,
                cards = Card.unserializeList(player1Cards),
                chips = player1Chips,
                isActive = player1IsActive,
                isDialer = dealerIndex == 1,
                lastDraw = ActionType.unserialize(player1LastDraw),
                lastBet = ActionType.unserialize(player1LastBet)
            )
            val player2 = PlayerData(
                name = player2Name,
                cards = Card.unserializeList(player2Cards),
                chips = player2Chips,
                isActive = player2IsActive,
                isDialer = dealerIndex == 2,
                lastDraw = ActionType.unserialize(player2LastDraw),
                lastBet = ActionType.unserialize(player2LastBet)
            )
            val player3 = PlayerData(
                name = player3Name,
                cards = Card.unserializeList(player3Cards),
                chips = player3Chips,
                isActive = player3IsActive,
                isDialer = dealerIndex == 3,
                lastDraw = ActionType.unserialize(player3LastDraw),
                lastBet = ActionType.unserialize(player3LastBet)
            )
            ScreenState(
                players = listOf(player0, player1, player2, player3),
                actionsAvailable = emptyList(),
                bankChips = bankChips,
                isDrawEnabled = false,
                isActionAvailable = true,
                isDealAvailable = true,
                isResetAvailable = isResetAvailable,
                isCardsOpen = false
            )
        }

        return SavedState(
            screenState = screenState,
            currentBet = localData.currentBet,
            numOfRaise = localData.numOfRaise,
            playerIndex = localData.playerIndex,
            round = RoundType.entries[localData.round],
            deck = Card.unserializeList(localData.deck)
        )
    }

    private fun loadSavedState(): SavedState =
        try {
            restoreSnapshot()
        } catch(_: Exception) {
            log("Local data is broken. Game was restarted")
            localData.resetGame()
            restoreSnapshot()
        }

    private fun player(index: Int) = _state.value.players[index]

    private fun log(mess: String) = Log.e("GamePlay", mess)

    private suspend fun logAndShow(mess: String) {
        log(mess)
        _events.emit(UiEvent.ShowToast(mess))
    }
}

enum class RoundType {
    PRE_DRAW,
    DRAW,
    POST_DRAW
}

class SavedState(
    val screenState: ScreenState,
    val currentBet: Int,
    val numOfRaise: Int,
    val playerIndex: Int,
    val round: RoundType,
    val deck: List<Card>,
)