package com.example.cardgame

object History {
    val list = mutableListOf<Pair<Int, ActionType>>()

    fun clear() = list.clear()

    fun add(index: Int, action :ActionType) = list.add(index to action)

    fun isAggressiveTable(): Boolean = list.count { it.second is ActionType.Raise } > 1
}