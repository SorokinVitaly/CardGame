package com.example.cardgame

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object SharedPreferencesManager {
    val prefs: SharedPreferences by lazy {
        ApplicationResourceManager.appContext.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            MODE_PRIVATE
        )
    }
    const val SHARED_PREFERENCES_NAME = "Preferences"
}

class PreferencesDelegate<T>(val keyName: String, val defaultValue: T) : ReadWriteProperty<Any?, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        with(SharedPreferencesManager.prefs) {
            when (defaultValue) {
                is Boolean -> getBoolean(keyName, defaultValue)
                is Int -> getInt(keyName, defaultValue)
                is Long -> getLong(keyName, defaultValue)
                is Float -> getFloat(keyName, defaultValue)
                is String -> getString(keyName, defaultValue)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        } as T

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        SharedPreferencesManager.prefs.edit {
            when (value) {
                is Boolean -> putBoolean(keyName, value)
                is Int -> putInt(keyName, value)
                is Long -> putLong(keyName, value)
                is Float -> putFloat(keyName, value)
                is String -> putString(keyName, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }
}