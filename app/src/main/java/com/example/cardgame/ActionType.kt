package com.example.cardgame

sealed class ActionType(val name: String, val bet: Int = 0) {
    class Bet(bet: Int) : ActionType("Bet $bet", bet)
    class Call(bet: Int) : ActionType("Call $bet", bet)
    class Raise(bet: Int) : ActionType("Raise $bet", bet)
    class Check : ActionType("Check")
    class Fold : ActionType("Fold")
    class Draw(val number: Int) : ActionType("Draw $number")
    class NoAction : ActionType("")
}