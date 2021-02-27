package com.example.musicplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicplayer.*
import org.greenrobot.eventbus.EventBus

class NotificationReciever :BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
//        MyService.play()
        EventBus.getDefault().post(ToggleSong())
    }

}

class NotificationRecieverBack :BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        EventBus.getDefault().post(PlayBackSong())
//        MyService.back()
    }

}

class NotificationRecieverNext :BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        EventBus.getDefault().post(PlayNextSong())
//        MyService.next()
    }

}
class NotificationRecieverClose :BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        val int=Intent(p0,MyService::class.java)
        p0!!.stopService(int)
    }

}

class NotificationRecieverMain :BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
        val intent=Intent(p0, MainActivity::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or( Intent.FLAG_ACTIVITY_NEW_TASK)
        p0!!.startActivity(intent)
    }
}