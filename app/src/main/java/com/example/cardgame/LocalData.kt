package com.example.cardgame

import android.content.SharedPreferences


interface LocalDataRepository {
    var userName: String
}

object LocalData : LocalDataRepository {
    override var userName: String
        get() = TODO("Not yet implemented")
        set(value) {}




/*
    private fun getEncryptedSharedPreferences(recreate: Boolean): SharedPreferences {
        val appContext = ApplicationResourceManager.appContext,
        return try {
            SharedPreferences.create(
                appContext,
                SHARED_PREFERENCES_NAME,
                MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch(e: Exception) {
            if (!recreate) throw e
            appContext.deleteSharedPreferences(SHARED_PREFERENCES_NAME)
            getEncryptedSharedPreferences(false)
        }
    }
*/
    const val SHARED_PREFERENCES_NAME = "Preferences"
}