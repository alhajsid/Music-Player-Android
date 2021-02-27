package com.example.musicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.musicplayer.adaptor.SongListAdaptor
import com.example.musicplayer.fragment.SongListFragment.Companion.adaptor
import com.example.musicplayer.model.AudioModel
import com.example.musicplayer.receiver.*
import com.example.musicplayer.utils.currentSeconds
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class MyService : Service() {

    var songname: String = ""

    val mSeekbarUpdateHandler = Handler()
    var mUpdateSeekbar: Runnable? = null
    var context: Context? = null

    var mediaPlayer: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build())
    }

    var playingSongIndex = 0

    var currentPlayingSong: AudioModel? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mUpdateSeekbar = object : Runnable {
            override fun run() {
                EventBus.getDefault().post(UpdateTime( mediaPlayer.currentSeconds,
                    mediaPlayer.seconds,mediaPlayer.currentPosition,context!!))
                mSeekbarUpdateHandler.postDelayed(this, 1000)
            }
        }

        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar!!, 200)

        mediaPlayer.setOnCompletionListener {
            try {
                if (playingSongIndex < songList!!.size - 1) {
                    playingSongIndex += 1
                    val obj = songList!![playingSongIndex]
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(obj.Path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    currentPlayingSong = obj
                } else {
                    playingSongIndex = 0
                    val obj = songList!![playingSongIndex]
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(obj.Path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    currentPlayingSong = obj
                }
                refresh(context!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        EventBus.getDefault().register(this)
        context = this
        try {
            songname = intent!!.getStringExtra("songname")!!.toLowerCase(Locale.getDefault())
        } catch (e: Exception) {
            Log.e("erroe", e.toString())
        }
        init()

        return START_NOT_STICKY
    }


    private fun init() {
        if (songname == "pause") {
            mediaPlayer.pause()
            refresh(context!!)
            return
        } else if (songname == "play") {
            mediaPlayer.start()
            refresh(context!!)
            return
        } else if (songname == "next") {
            next(context!!)
            refresh(context!!)
            return
        } else if (songname == "back") {
            back(context!!)
            refresh(context!!)
            return
        } else if (songname.length > 1) {
            getAllAudioFromDevice(this, 1)
            isServiceRunning = true
            startForgroundmService()
            return
        } else {
            Log.e("my service", "get all songs")
            getAllAudioFromDevice(context!!, 0)
        }
        isServiceRunning = true
        startForgroundmService()

    }


    fun startForgroundmService() {
        val collapseview = RemoteViews(packageName, R.layout.notification_layout)

        val clickintent = Intent(applicationContext, NotificationReciever::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, clickintent, 0)

        val clickintent1 = Intent(applicationContext, NotificationRecieverBack::class.java)
        val pi1 = PendingIntent.getBroadcast(this, 0, clickintent1, 0)

        val clickintent2 = Intent(applicationContext, NotificationRecieverNext::class.java)
        val pi2 = PendingIntent.getBroadcast(this, 0, clickintent2, 0)

        val clickintent3 = Intent(applicationContext, NotificationRecieverMain::class.java)
        val pi3 = PendingIntent.getBroadcast(this, 0, clickintent3, 0)

        val clickintent4 = Intent(applicationContext, NotificationRecieverClose::class.java)
        val pi4 = PendingIntent.getBroadcast(this, 0, clickintent4, 0)

        val clickintent5 = Intent(applicationContext, MainActivity::class.java)
        clickintent5.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        clickintent5.putExtra("NotificationMessage", "I am from Notification")
        clickintent5.addCategory(Intent.CATEGORY_LAUNCHER)
        clickintent5.action = Intent.ACTION_MAIN
        val pi5 = PendingIntent.getActivity(this, 0, clickintent5, 0)

        collapseview.setOnClickPendingIntent(R.id.iv_play_pause, pi)
        collapseview.setOnClickPendingIntent(R.id.iv_back_song, pi1)
        collapseview.setOnClickPendingIntent(R.id.iv_next_song, pi2)
        collapseview.setOnClickPendingIntent(R.id.main_container, pi3)
        collapseview.setOnClickPendingIntent(R.id.iv_close, pi4)
        collapseview.setOnClickPendingIntent(R.id.tv_playing_song, pi5)

        val notification = NotificationCompat.Builder(this, App().CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_headset_24)
            .setCustomContentView(collapseview)
            .setContentIntent(pi5)
            .build()
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun getAllAudioFromDevice(context: Context, a: Int) {
        val aj = MediaPlayer()
        val tempAudioList = ArrayList<AudioModel>()
        if (!isServiceRunning) {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST
            )
            val c = context.contentResolver.query(
                uri,
                projection,
                null, null, null
            )

            if (c != null) {
                while (c.moveToNext()) {
                    val audioModel = AudioModel()
                    val path = c.getString(0)
                    val name = c.getString(1)
                    val album = c.getString(2)
                    val artist = c.getString(3)
                    aj.reset()
                    aj.setDataSource(path)
                    aj.prepare()
                    if (aj.duration >= 60000) {
                        audioModel.Name=(name)
                        audioModel.Album=(album)
                        audioModel.Artist=(artist)
                        audioModel.Path=(path)
                        audioModel.time=(aj.seconds)
                        tempAudioList.add(audioModel)
                        //adaptor.add(usersiten1(audioModel))
                    }
                }
                c.close()
            }
            if (tempAudioList.size != 0) {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(tempAudioList[0].Path)
                currentPlayingSong = tempAudioList.get(0)
                mediaPlayer.prepare()
                mediaPlayer.pause()

                SongListAdaptor.SongList=tempAudioList
                adaptor.notifyDataSetChanged()
            }
            if (songList != null) {
                songList!!.clear()
            }
            songList = tempAudioList

        }

        if (a == 1) {
            Log.e("error11", songname)
            for (i in 0 until songList!!.size) {
                if (songList!![i].Name.toLowerCase(Locale.ROOT).indexOf(songname) != -1) {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(songList!![i].Path)
                    currentPlayingSong = songList!![i]
                    playingSongIndex = i
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    refresh(context)
                    return
                }
            }
        }
        return
    }

    fun refreshnotification(context: Context) {
        val collapseview = RemoteViews("com.example.musicplayer", R.layout.notification_layout)

        val clickintent = Intent(context, NotificationReciever::class.java)

        val pi = PendingIntent.getBroadcast(context, 0, clickintent, 0)

        val clickintent1 = Intent(context, NotificationRecieverBack::class.java)
        val pi1 = PendingIntent.getBroadcast(context, 0, clickintent1, 0)

        val clickintent2 = Intent(context, NotificationRecieverNext::class.java)
        val pi2 = PendingIntent.getBroadcast(context, 0, clickintent2, 0)

        val clickintent3 = Intent(context, NotificationRecieverMain::class.java)
        val pi3 = PendingIntent.getBroadcast(context, 0, clickintent3, 0)

        val clickintent4 = Intent(context, NotificationRecieverClose::class.java)
        val pi4 = PendingIntent.getBroadcast(context, 0, clickintent4, 0)


        val clickintent5 = Intent(context, MainActivity::class.java)
        clickintent5.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        clickintent5.putExtra("NotificationMessage", "I am from Notification")
        clickintent5.addCategory(Intent.CATEGORY_LAUNCHER)
        clickintent5.action = Intent.ACTION_MAIN
        val pi5 = PendingIntent.getActivity(context, 0, clickintent5, 0)

        collapseview.setOnClickPendingIntent(R.id.iv_play_pause, pi)
        collapseview.setOnClickPendingIntent(R.id.iv_back_song, pi1)
        collapseview.setOnClickPendingIntent(R.id.iv_next_song, pi2)
        collapseview.setOnClickPendingIntent(R.id.main_container, pi3)
        collapseview.setOnClickPendingIntent(R.id.iv_close, pi4)
        collapseview.setOnClickPendingIntent(R.id.tv_playing_song, pi5)

        collapseview.setTextViewText(R.id.tv_playing_song, currentPlayingSong?.Name)

        if (mediaPlayer.isPlaying) {
            collapseview.setImageViewResource(R.id.iv_play_pause, R.drawable.btn_play_song)
        } else {
            collapseview.setImageViewResource(R.id.iv_play_pause, R.drawable.btn_pause_song)
        }

        val notification = NotificationCompat.Builder(context, App().CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_headset_24)
            .setCustomContentView(collapseview)
            .setContentIntent(pi5)
            .build()
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(2, notification)

    }

    fun play(context: Context) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        refresh(context)
    }

    fun refresh(context: Context) {
        EventBus.getDefault().post(SongChanged(currentPlayingSong!!, mediaPlayer.isPlaying,
            mediaPlayer.duration,context))
        MyService().refreshnotification(context)
    }

    fun back(context: Context) {
        if (playingSongIndex > 0) {
            playingSongIndex -= 1
            mediaPlayer.reset()
            val obj = songList!![playingSongIndex]
            mediaPlayer.setDataSource(obj.Path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            currentPlayingSong = obj
        }
        refresh(context)
    }

    fun next(context: Context) {
        try {
            if (playingSongIndex < songList!!.size - 1) {
                playingSongIndex += 1
                mediaPlayer.reset()
                val obj = songList!![playingSongIndex]
                mediaPlayer.setDataSource(obj.Path)
                mediaPlayer.prepare()
                mediaPlayer.start()
                currentPlayingSong = obj
                refresh(context)
            }
        } catch (e: Exception) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun playEvent(event:ToggleSong){
        play(event.context)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun SeekToEvent(event:SeekTo){
        mediaPlayer.seekTo(event.position)
        refresh(event.context)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateEvent(event:UpdateData){
        refresh(event.context)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun NextEvent(event:PlayNextSong){
        next(event.context)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun BackEvent(event:PlayBackSong){
        back(event.context)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun PlayAtEvent(event:PlaySongAt?){
        mediaPlayer.reset()
        val obj = songList!![event!!.position]
        mediaPlayer.setDataSource(obj.Path)
        mediaPlayer.prepare()
        mediaPlayer.start()
        currentPlayingSong = obj
        playingSongIndex = event.position
        refresh(context!!)
    }

    companion object {
        var songList: ArrayList<AudioModel>? = null
        var isServiceRunning = false
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
}
