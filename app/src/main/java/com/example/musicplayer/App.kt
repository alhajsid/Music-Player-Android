package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App: Application(){

    val CHANNEL_ID="MUSIC_PLAYER_AJ"

    override fun onCreate() {
        super.onCreate()
        createnotificationchannel()
    }

    fun createnotificationchannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Main CHANNEL",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager=getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}