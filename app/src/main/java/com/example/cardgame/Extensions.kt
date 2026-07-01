package com.example.cardgame

fun String.splitItems(delimiter: Char = ','): List<String> =
    substring(1, length - 1).split(delimiter).map { it.trim() }