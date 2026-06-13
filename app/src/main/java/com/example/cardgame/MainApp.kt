package com.example.cardgame

import android.app.Application
import coil.ImageLoader
import coil.decode.SvgDecoder


class MainApp : Application() {
    val imageLoader by lazy {
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationResourceManager.init(this)
    }
}