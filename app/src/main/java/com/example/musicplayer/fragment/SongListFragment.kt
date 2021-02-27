package com.example.musicplayer.fragment

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicplayer.*
import com.example.musicplayer.adaptor.SongListAdaptor
import kotlinx.android.synthetic.main.fragment_song_list.*
import kotlinx.android.synthetic.main.fragment_song_list.iv_back_song
import kotlinx.android.synthetic.main.fragment_song_list.iv_next_song
import kotlinx.android.synthetic.main.fragment_song_list.iv_play_pause
import kotlinx.android.synthetic.main.fragment_song_list.sb_progress
import kotlinx.android.synthetic.main.fragment_song_list.tv_song_name
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SongListFragment : Fragment(R.layout.fragment_song_list) {

    companion object {
        lateinit var adaptor: SongListAdaptor
    }

    var currentPlayingSongPath=""

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChanged(event: SongChanged?) {
        event!!.songModel
        if (event.isPlaying) {
            Glide.with(context!!).load(R.drawable.btn_play_song).into(iv_play_pause)
        } else {
            Glide.with(context!!).load(R.drawable.btn_pause_song).into(iv_play_pause)
        }
        sb_progress.max = event.duration
        tv_song_name.text = event.songModel.Name
        adaptor.currentPlayingSongPath=event.songModel.Path
        adaptor.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChanged(event: UpdateTime?) {
        sb_progress.progress = event!!.progress
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().post(UpdateData())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventBus.getDefault().register(this)

        tv_song_name.isSelected = true
        initRecyclerView()

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initView()
        }
    }

    fun initView(){

        if (MyService.isServiceRunning) {
            setUpDataFromService()
        } else {
            startService()
        }
        clickListeners()
    }

    private fun setUpDataFromService() {
        if (MyService.songList!!.size != 0) {
            adaptor.setList(MyService.songList!!)
        }
        adaptor.notifyDataSetChanged()
        EventBus.getDefault().post(UpdateData())
    }

    fun initRecyclerView(){
        adaptor = SongListAdaptor()
        rv_song_list.adapter = adaptor
    }

    private fun startService() {
        context!!.startService(Intent(context!!, MyService::class.java))
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context!!.registerReceiver(MyReceiver(), filter)
    }

    fun clickListeners(){

        iv_setting.setOnClickListener { startActivity(Intent(context,SettingActivity::class.java)) }

        smallsongpla.setOnClickListener {
            (activity as MainActivity).mPager.setCurrentItem(1, true)
        }

        iv_play_pause.setOnClickListener {
            EventBus.getDefault().post(ToggleSong())
        }

        iv_next_song.setOnClickListener {
            EventBus.getDefault().post(PlayNextSong())
        }

        iv_back_song.setOnClickListener {
            EventBus.getDefault().post(PlayBackSong())
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}

  