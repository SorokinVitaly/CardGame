package com.example.cardgame

import android.annotation.SuppressLint
import android.content.Context

// Here I save application context and not activity context, so there will be no memory leak
@SuppressLint("StaticFieldLeak")
object ApplicationResourceManager {
    lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}