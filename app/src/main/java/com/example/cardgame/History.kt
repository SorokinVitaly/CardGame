package com.example.cardgame

import javax.inject.Inject


interface History {
    val list: List<Pair<Int, ActionType>>
    fun clear()
    fun add(index: Int, action :ActionType)
    fun isAggressiveTable(): Boolean
    fun serialize(): String
    fun unserialize(saved: String)
}

class HistoryImpl @Inject constructor() : History  {
    override val list = mutableListOf<Pair<Int, ActionType>>()

    override fun clear() = list.clear()

    override fun add(index: Int, action :ActionType) { list.add(index to action) }

    override fun isAggressiveTable(): Boolean = list.count { it.second is ActionType.Raise } > 1

    override fun serialize(): String =
        if (list.isEmpty()) {
            ""
        } else {
            list.joinToString(prefix = "[", postfix = "]") {
                "(${it.first};${it.second.serialize()})"
            }
        }

    override fun unserialize(saved: String) {
        list.clear()
        if (saved.isNotEmpty()) {
            list.addAll(
                saved.splitItems().map {
                    val itemList = it.splitItems(';')
                    require(itemList.size == 2)
                    itemList[0].toInt() to ActionType.unserialize(itemList[1])
                }
            )
        }
    }
}