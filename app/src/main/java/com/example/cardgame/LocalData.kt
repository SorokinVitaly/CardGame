package com.example.cardgame


interface LocalDataRepository {
    var userName: String
    var userAge: Int
}

object LocalData : LocalDataRepository {
    override var userName: String by PreferencesDelegate(::userName.name, "")
    override var userAge: Int by PreferencesDelegate(::userAge.name, 17)
}