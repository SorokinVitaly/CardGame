package com.example.cardgame

sealed class BetType(val name: String) {
    class Bet(bet: Int) : BetType("Bet $bet")
    class Call(bet: Int) : BetType("Call $bet")
    class Raise(bet: Int) : BetType("Raise $bet")
    class Check : BetType("Check")
    class Fold : BetType("Fold")
    class NoBet : BetType("")
}