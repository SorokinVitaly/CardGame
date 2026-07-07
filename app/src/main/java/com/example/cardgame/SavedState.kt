package com.example.cardgame

class SavedState(
    val screenState: ScreenState,
    val currentBet: Int,
    val numOfRaise: Int,
    val playerIndex: Int,
    val round: RoundType,
    val deck: List<Card>,
)

fun saveSnapshot(
    localData: LocalDataRepository,
    history: History,
    savedState: SavedState
) {
    val players = savedState.screenState.players
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

        bankChips = savedState.screenState.bankChips
        isResetAvailable = savedState.screenState.isResetAvailable
        currentBet = savedState.currentBet
        numOfRaise = savedState.numOfRaise
        playerIndex = savedState.playerIndex
        round = savedState.round.ordinal
        deck = Card.serializeList(savedState.deck)
        localData.history = history.serialize()
    }
}

fun restoreSnapshot(
    localData: LocalDataRepository,
    history: History
): SavedState {
    history.unserialize(localData.history)

    val screenState = with (localData) {
        val player0 = PlayerData(
            name = player0Name,
            cards = Card.unserializeList(player0Cards),
            chips = player0Chips,
            isActive = player0IsActive,
            isDealer = dealerIndex == 0,
            lastDraw = ActionType.unserialize(player0LastDraw),
            lastBet = ActionType.unserialize(player0LastBet)
        )
        val player1 = PlayerData(
            name = player1Name,
            cards = Card.unserializeList(player1Cards),
            chips = player1Chips,
            isActive = player1IsActive,
            isDealer = dealerIndex == 1,
            lastDraw = ActionType.unserialize(player1LastDraw),
            lastBet = ActionType.unserialize(player1LastBet)
        )
        val player2 = PlayerData(
            name = player2Name,
            cards = Card.unserializeList(player2Cards),
            chips = player2Chips,
            isActive = player2IsActive,
            isDealer = dealerIndex == 2,
            lastDraw = ActionType.unserialize(player2LastDraw),
            lastBet = ActionType.unserialize(player2LastBet)
        )
        val player3 = PlayerData(
            name = player3Name,
            cards = Card.unserializeList(player3Cards),
            chips = player3Chips,
            isActive = player3IsActive,
            isDealer = dealerIndex == 3,
            lastDraw = ActionType.unserialize(player3LastDraw),
            lastBet = ActionType.unserialize(player3LastBet)
        )
        val players = listOf(player0, player1, player2, player3)
        val isDealAvailable = player0.isActive && players.count { it.isActive } > 2
        ScreenState(
            players = players,
            actionsAvailable = emptyList(),
            bankChips = bankChips,
            isDrawEnabled = false,
            isActionAvailable = true,
            isDealAvailable = isDealAvailable,
            isResetAvailable = isResetAvailable || !isDealAvailable,
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