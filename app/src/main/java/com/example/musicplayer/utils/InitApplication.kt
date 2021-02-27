package com.example.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences

class InitApplication(context: Context) {

    var sharedPreferences: SharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    var state: Boolean
        get() = sharedPreferences.getBoolean("THEME_MODE", true)
        set(bVal) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("THEME_MODE", bVal)
            editor.apply()
        }

}