package com.example.cardgame


interface LocalDataRepository {
    var player0Name: String
    var player1Name: String
    var player2Name: String
    var player3Name: String

    var player0Chips: Int
    var player1Chips: Int
    var player2Chips: Int
    var player3Chips: Int

    var isPlayer1Active: Boolean
    var isPlayer2Active: Boolean
    var isPlayer3Active: Boolean
}

object LocalData : LocalDataRepository {
    override var player0Name: String by PreferencesDelegate(
        ::player0Name.name,
        DEFAULT_PLAYER_0_NAME
    )
    override var player1Name: String by PreferencesDelegate(
        ::player1Name.name,
        DEFAULT_PLAYER_1_NAME
    )
    override var player2Name: String by PreferencesDelegate(
        ::player2Name.name,
        DEFAULT_PLAYER_2_NAME
    )
    override var player3Name: String by PreferencesDelegate(
        ::player3Name.name,
        DEFAULT_PLAYER_3_NAME
    )
    override var player0Chips: Int by PreferencesDelegate(
        ::player0Chips.name,
        DEFAULT_CHIP_NUMBER
    )
    override var player1Chips: Int by PreferencesDelegate(
        ::player1Chips.name,
        DEFAULT_CHIP_NUMBER
    )
    override var player2Chips: Int by PreferencesDelegate(
        ::player2Chips.name,
        DEFAULT_CHIP_NUMBER
    )
    override var player3Chips: Int by PreferencesDelegate(
        ::player3Chips.name,
        DEFAULT_CHIP_NUMBER
    )
    override var isPlayer1Active: Boolean by PreferencesDelegate(
        ::isPlayer1Active.name,
        true
    )
    override var isPlayer2Active: Boolean by PreferencesDelegate(
        ::isPlayer2Active.name,
        true
    )
    override var isPlayer3Active: Boolean by PreferencesDelegate(
        ::isPlayer3Active.name,
        true
    )

    const val DEFAULT_PLAYER_0_NAME = "Me"
    const val DEFAULT_PLAYER_1_NAME = "Lesley Colon"
    const val DEFAULT_PLAYER_2_NAME = "Leon Kim"
    const val DEFAULT_PLAYER_3_NAME = "Vanessa May"
    const val DEFAULT_CHIP_NUMBER = 1000
}