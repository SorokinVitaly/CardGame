package com.example.cardgame

import androidx.annotation.DrawableRes

enum class CardSuit {
    SPADES,
    CLUBS,
    DIAMONDS,
    HEARTS
}

enum class CardRank {
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
    val rank: CardRank,
    val suit: CardSuit,
    @DrawableRes val imageId: Int = 0
)

private val jokerCard = Card(CardRank.JOKER, CardSuit.SPADES, R.drawable.joker_3)

private val standardCards = setOf(
    Card(CardRank.TWO, CardSuit.SPADES, R.drawable.spade_2),
    Card(CardRank.THREE, CardSuit.SPADES, R.drawable.spade_3),
    Card(CardRank.FOUR, CardSuit.SPADES, R.drawable.spade_4),
    Card(CardRank.FIVE, CardSuit.SPADES, R.drawable.spade_5),
    Card(CardRank.SIX, CardSuit.SPADES, R.drawable.spade_6),
    Card(CardRank.SEVEN, CardSuit.SPADES, R.drawable.spade_7),
    Card(CardRank.EIGHT, CardSuit.SPADES, R.drawable.spade_8),
    Card(CardRank.NINE, CardSuit.SPADES, R.drawable.spade_9),
    Card(CardRank.TEN, CardSuit.SPADES, R.drawable.spade_10),
    Card(CardRank.JACK, CardSuit.SPADES, R.drawable.spade_11_jack),
    Card(CardRank.QUEEN, CardSuit.SPADES, R.drawable.spade_12_queen),
    Card(CardRank.KING, CardSuit.SPADES, R.drawable.spade_13_king),
    Card(CardRank.ACE, CardSuit.SPADES, R.drawable.spade_1),

    Card(CardRank.TWO, CardSuit.CLUBS, R.drawable.club_2),
    Card(CardRank.THREE, CardSuit.CLUBS, R.drawable.club_3),
    Card(CardRank.FOUR, CardSuit.CLUBS, R.drawable.club_4),
    Card(CardRank.FIVE, CardSuit.CLUBS, R.drawable.club_5),
    Card(CardRank.SIX, CardSuit.CLUBS, R.drawable.club_6),
    Card(CardRank.SEVEN, CardSuit.CLUBS, R.drawable.club_7),
    Card(CardRank.EIGHT, CardSuit.CLUBS, R.drawable.club_8),
    Card(CardRank.NINE, CardSuit.CLUBS, R.drawable.club_9),
    Card(CardRank.TEN, CardSuit.CLUBS, R.drawable.club_10),
    Card(CardRank.JACK, CardSuit.CLUBS, R.drawable.club_11_jack),
    Card(CardRank.QUEEN, CardSuit.CLUBS, R.drawable.club_12_queen),
    Card(CardRank.KING, CardSuit.CLUBS, R.drawable.club_13_king),
    Card(CardRank.ACE, CardSuit.CLUBS, R.drawable.club_1),

    Card(CardRank.TWO, CardSuit.DIAMONDS, R.drawable.diamond_2),
    Card(CardRank.THREE, CardSuit.DIAMONDS, R.drawable.diamond_3),
    Card(CardRank.FOUR, CardSuit.DIAMONDS, R.drawable.diamond_4),
    Card(CardRank.FIVE, CardSuit.DIAMONDS, R.drawable.diamond_5),
    Card(CardRank.SIX, CardSuit.DIAMONDS, R.drawable.diamond_6),
    Card(CardRank.SEVEN, CardSuit.DIAMONDS, R.drawable.diamond_7),
    Card(CardRank.EIGHT, CardSuit.DIAMONDS, R.drawable.diamond_8),
    Card(CardRank.NINE, CardSuit.DIAMONDS, R.drawable.diamond_9),
    Card(CardRank.TEN, CardSuit.DIAMONDS, R.drawable.diamond_10),
    Card(CardRank.JACK, CardSuit.DIAMONDS, R.drawable.diamond_11_jack),
    Card(CardRank.QUEEN, CardSuit.DIAMONDS, R.drawable.diamond_12_queen),
    Card(CardRank.KING, CardSuit.DIAMONDS, R.drawable.diamond_13_king),
    Card(CardRank.ACE, CardSuit.DIAMONDS, R.drawable.diamond_1),

    Card(CardRank.TWO, CardSuit.HEARTS, R.drawable.heart_2),
    Card(CardRank.THREE, CardSuit.HEARTS, R.drawable.heart_3),
    Card(CardRank.FOUR, CardSuit.HEARTS, R.drawable.heart_4),
    Card(CardRank.FIVE, CardSuit.HEARTS, R.drawable.heart_5),
    Card(CardRank.SIX, CardSuit.HEARTS, R.drawable.heart_6),
    Card(CardRank.SEVEN, CardSuit.HEARTS, R.drawable.heart_7),
    Card(CardRank.EIGHT, CardSuit.HEARTS, R.drawable.heart_8),
    Card(CardRank.NINE, CardSuit.HEARTS, R.drawable.heart_9),
    Card(CardRank.TEN, CardSuit.HEARTS, R.drawable.heart_10),
    Card(CardRank.JACK, CardSuit.HEARTS, R.drawable.heart_11_jack),
    Card(CardRank.QUEEN, CardSuit.HEARTS, R.drawable.heart_12_queen),
    Card(CardRank.KING, CardSuit.HEARTS, R.drawable.heart_13_king),
    Card(CardRank.ACE, CardSuit.HEARTS, R.drawable.heart_1),
).toList()

val deckBlackJack = standardCards

val deckPoker = standardCards

val deckPokerWithJokers = standardCards + jokerCard + jokerCard

val deckPreferans = standardCards.filter { it.rank >= CardRank.SEVEN }

val deckDurak = standardCards.filter { it.rank >= CardRank.SIX }