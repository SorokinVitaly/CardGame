package com.example.cardgame

sealed class BetType(val name: String, val bet: Int = 0) {
    class Bet(bet: Int) : BetType("Bet $bet", bet)
    class Call(bet: Int) : BetType("Call $bet", bet)
    class Raise(bet: Int) : BetType("Raise $bet", bet)
    class Check : BetType("Check")
    class Fold : BetType("Fold")
    class NoBet : BetType("")
}