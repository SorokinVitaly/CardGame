package com.example.cardgame

import androidx.annotation.DrawableRes

enum class CardSuit {
    SPADES,
    CLUBS,
    DIAMONDS,
    HEARTS
}

enum class CardValue {
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE,
    JOKER
}

data class Card(
    val value: CardValue,
    val suit: CardSuit,
    @DrawableRes val imageId: Int = 0
)

private val jokerCard = Card(CardValue.JOKER, CardSuit.SPADES, 0)

private val standardCards = setOf(
    Card(CardValue.TWO, CardSuit.SPADES, 0),
    Card(CardValue.THREE, CardSuit.SPADES, 0),
    Card(CardValue.FOUR, CardSuit.SPADES, 0),
    Card(CardValue.FIVE, CardSuit.SPADES, 0),
    Card(CardValue.SIX, CardSuit.SPADES, 0),
    Card(CardValue.SEVEN, CardSuit.SPADES, 0),
    Card(CardValue.EIGHT, CardSuit.SPADES, 0),
    Card(CardValue.NINE, CardSuit.SPADES, 0),
    Card(CardValue.TEN, CardSuit.SPADES, 0),
    Card(CardValue.JACK, CardSuit.SPADES, 0),
    Card(CardValue.QUEEN, CardSuit.SPADES, 0),
    Card(CardValue.KING, CardSuit.SPADES, 0),
    Card(CardValue.ACE, CardSuit.SPADES, 0),

    Card(CardValue.TWO, CardSuit.CLUBS, 0),
    Card(CardValue.THREE, CardSuit.CLUBS, 0),
    Card(CardValue.FOUR, CardSuit.CLUBS, 0),
    Card(CardValue.FIVE, CardSuit.CLUBS, 0),
    Card(CardValue.SIX, CardSuit.CLUBS, 0),
    Card(CardValue.SEVEN, CardSuit.CLUBS, 0),
    Card(CardValue.EIGHT, CardSuit.CLUBS, 0),
    Card(CardValue.NINE, CardSuit.CLUBS, 0),
    Card(CardValue.TEN, CardSuit.CLUBS, 0),
    Card(CardValue.JACK, CardSuit.CLUBS, 0),
    Card(CardValue.QUEEN, CardSuit.CLUBS, 0),
    Card(CardValue.KING, CardSuit.CLUBS, 0),
    Card(CardValue.ACE, CardSuit.CLUBS, 0),

    Card(CardValue.TWO, CardSuit.DIAMONDS, 0),
    Card(CardValue.THREE, CardSuit.DIAMONDS, 0),
    Card(CardValue.FOUR, CardSuit.DIAMONDS, 0),
    Card(CardValue.FIVE, CardSuit.DIAMONDS, 0),
    Card(CardValue.SIX, CardSuit.DIAMONDS, 0),
    Card(CardValue.SEVEN, CardSuit.DIAMONDS, 0),
    Card(CardValue.EIGHT, CardSuit.DIAMONDS, 0),
    Card(CardValue.NINE, CardSuit.DIAMONDS, 0),
    Card(CardValue.TEN, CardSuit.DIAMONDS, 0),
    Card(CardValue.JACK, CardSuit.DIAMONDS, 0),
    Card(CardValue.QUEEN, CardSuit.DIAMONDS, 0),
    Card(CardValue.KING, CardSuit.DIAMONDS, 0),
    Card(CardValue.ACE, CardSuit.DIAMONDS, 0),

    Card(CardValue.TWO, CardSuit.HEARTS, 0),
    Card(CardValue.THREE, CardSuit.HEARTS, 0),
    Card(CardValue.FOUR, CardSuit.HEARTS, 0),
    Card(CardValue.FIVE, CardSuit.HEARTS, 0),
    Card(CardValue.SIX, CardSuit.HEARTS, 0),
    Card(CardValue.SEVEN, CardSuit.HEARTS, 0),
    Card(CardValue.EIGHT, CardSuit.HEARTS, 0),
    Card(CardValue.NINE, CardSuit.HEARTS, 0),
    Card(CardValue.TEN, CardSuit.HEARTS, 0),
    Card(CardValue.JACK, CardSuit.HEARTS, 0),
    Card(CardValue.QUEEN, CardSuit.HEARTS, 0),
    Card(CardValue.KING, CardSuit.HEARTS, 0),
    Card(CardValue.ACE, CardSuit.HEARTS, 0),
).toList()

val deckBlackJack = standardCards

val deckPoker = standardCards

val deckPokerWithJokers = standardCards + jokerCard + jokerCard

val deckPreferans = standardCards.filter { it.value >= CardValue.SEVEN }

val deckDurak = standardCards.filter { it.value >= CardValue.SIX }