package com.example.cardgame

sealed class ActionType(
    val name: String,
    val payNow: Int = 0,
    val paid: Int = 0
) {
    class Check(currentBet: Int = 0) : ActionType(name = "Check", paid = currentBet)
    class Bet(bet: Int) : ActionType(name = "Bet $bet",payNow = bet, paid = bet)

    class Call(currentBet: Int, prevPaid: Int) : ActionType(
        name = "Call ${currentBet - prevPaid}",
        payNow = currentBet - prevPaid,
        paid = currentBet
    )

    class Raise(raiseTo: Int, prevPaid: Int) : ActionType(
        name = "Raise to $raiseTo",
        payNow = raiseTo - prevPaid,
        paid = raiseTo
    )

    class Draw(val number: Int = 0) : ActionType("Draw")
    class Fold : ActionType("Fold")
    class NoAction : ActionType("")
}