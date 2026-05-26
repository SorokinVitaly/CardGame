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

private val jokerCard = Card(CardValue.JOKER, CardSuit.SPADES, R.drawable.joker_3)

private val standardCards = setOf(
    Card(CardValue.TWO, CardSuit.SPADES, R.drawable.spade_2),
    Card(CardValue.THREE, CardSuit.SPADES, R.drawable.spade_3),
    Card(CardValue.FOUR, CardSuit.SPADES, R.drawable.spade_4),
    Card(CardValue.FIVE, CardSuit.SPADES, R.drawable.spade_5),
    Card(CardValue.SIX, CardSuit.SPADES, R.drawable.spade_6),
    Card(CardValue.SEVEN, CardSuit.SPADES, R.drawable.spade_7),
    Card(CardValue.EIGHT, CardSuit.SPADES, R.drawable.spade_8),
    Card(CardValue.NINE, CardSuit.SPADES, R.drawable.spade_9),
    Card(CardValue.TEN, CardSuit.SPADES, R.drawable.spade_10),
    Card(CardValue.JACK, CardSuit.SPADES, R.drawable.spade_11_jack),
    Card(CardValue.QUEEN, CardSuit.SPADES, R.drawable.spade_12_queen),
    Card(CardValue.KING, CardSuit.SPADES, R.drawable.spade_13_king),
    Card(CardValue.ACE, CardSuit.SPADES, R.drawable.spade_1),

    Card(CardValue.TWO, CardSuit.CLUBS, R.drawable.club_2),
    Card(CardValue.THREE, CardSuit.CLUBS, R.drawable.club_3),
    Card(CardValue.FOUR, CardSuit.CLUBS, R.drawable.club_4),
    Card(CardValue.FIVE, CardSuit.CLUBS, R.drawable.club_5),
    Card(CardValue.SIX, CardSuit.CLUBS, R.drawable.club_6),
    Card(CardValue.SEVEN, CardSuit.CLUBS, R.drawable.club_7),
    Card(CardValue.EIGHT, CardSuit.CLUBS, R.drawable.club_8),
    Card(CardValue.NINE, CardSuit.CLUBS, R.drawable.club_9),
    Card(CardValue.TEN, CardSuit.CLUBS, R.drawable.club_10),
    Card(CardValue.JACK, CardSuit.CLUBS, R.drawable.club_11_jack),
    Card(CardValue.QUEEN, CardSuit.CLUBS, R.drawable.club_12_queen),
    Card(CardValue.KING, CardSuit.CLUBS, R.drawable.club_13_king),
    Card(CardValue.ACE, CardSuit.CLUBS, R.drawable.club_1),

    Card(CardValue.TWO, CardSuit.DIAMONDS, R.drawable.diamond_2),
    Card(CardValue.THREE, CardSuit.DIAMONDS, R.drawable.diamond_3),
    Card(CardValue.FOUR, CardSuit.DIAMONDS, R.drawable.diamond_4),
    Card(CardValue.FIVE, CardSuit.DIAMONDS, R.drawable.diamond_5),
    Card(CardValue.SIX, CardSuit.DIAMONDS, R.drawable.diamond_6),
    Card(CardValue.SEVEN, CardSuit.DIAMONDS, R.drawable.diamond_7),
    Card(CardValue.EIGHT, CardSuit.DIAMONDS, R.drawable.diamond_8),
    Card(CardValue.NINE, CardSuit.DIAMONDS, R.drawable.diamond_9),
    Card(CardValue.TEN, CardSuit.DIAMONDS, R.drawable.diamond_10),
    Card(CardValue.JACK, CardSuit.DIAMONDS, R.drawable.diamond_11_jack),
    Card(CardValue.QUEEN, CardSuit.DIAMONDS, R.drawable.diamond_12_queen),
    Card(CardValue.KING, CardSuit.DIAMONDS, R.drawable.diamond_13_king),
    Card(CardValue.ACE, CardSuit.DIAMONDS, R.drawable.diamond_1),

    Card(CardValue.TWO, CardSuit.HEARTS, R.drawable.heart_2),
    Card(CardValue.THREE, CardSuit.HEARTS, R.drawable.heart_3),
    Card(CardValue.FOUR, CardSuit.HEARTS, R.drawable.heart_4),
    Card(CardValue.FIVE, CardSuit.HEARTS, R.drawable.heart_5),
    Card(CardValue.SIX, CardSuit.HEARTS, R.drawable.heart_6),
    Card(CardValue.SEVEN, CardSuit.HEARTS, R.drawable.heart_7),
    Card(CardValue.EIGHT, CardSuit.HEARTS, R.drawable.heart_8),
    Card(CardValue.NINE, CardSuit.HEARTS, R.drawable.heart_9),
    Card(CardValue.TEN, CardSuit.HEARTS, R.drawable.heart_10),
    Card(CardValue.JACK, CardSuit.HEARTS, R.drawable.heart_11_jack),
    Card(CardValue.QUEEN, CardSuit.HEARTS, R.drawable.heart_12_queen),
    Card(CardValue.KING, CardSuit.HEARTS, R.drawable.heart_13_king),
    Card(CardValue.ACE, CardSuit.HEARTS, R.drawable.heart_1),
).toList()

val deckBlackJack = standardCards

val deckPoker = standardCards

val deckPokerWithJokers = standardCards + jokerCard + jokerCard

val deckPreferans = standardCards.filter { it.value >= CardValue.SEVEN }

val deckDurak = standardCards.filter { it.value >= CardValue.SIX }