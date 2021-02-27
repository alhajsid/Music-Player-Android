package com.example.musicplayer.utils

import android.media.MediaPlayer

val MediaPlayer.currentSeconds:String
    get() {
        var seconds1=this.currentPosition / 1000
        var i=0
        while (seconds1>59){
            i += 1
            seconds1 -= 60
        }
        var o=""
        o = if(seconds1<10){
            "$i:0$seconds1"
        }else{
            "$i:$seconds1"

        }
        return o
    }


private val MediaPlayer.seconds: String
    get() {
        var seconds1 = this.duration / 1000
        var i = 0
        while (seconds1 > 59) {
            i += 1
            seconds1 -= 60
        }
        return if (seconds1 < 10) {
            "$i:0$seconds1"
        } else {
            "$i:$seconds1"
        }
    }