package com.tertiaryinfotech.freefood

import android.app.Application
import org.osmdroid.config.Configuration

class FreeFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // osmdroid needs a user-agent and a cache path before any MapView is created.
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = filesDir
            osmdroidTileCache = filesDir.resolve("osmdroid-tiles")
        }
    }
}
