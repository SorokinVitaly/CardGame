package com.example.cardgame

sealed class LastBet(val name: String) {
    class Bet(bet: Int) : LastBet("Bet $bet")
    class Call(bet: Int) : LastBet("Call $bet")
    class Raise(bet: Int) : LastBet("Raise $bet")
    class Check : LastBet("Check")
    class Fold : LastBet("Fold")
    class NoBet : LastBet("")
}