package com.example.musicplayer.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicplayer.*
import kotlinx.android.synthetic.main.fragment_song_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SongDetailFragment : Fragment(R.layout.fragment_song_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        tv_song_name.isSelected=true
        initView()
        clickListeners()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().post(UpdateData())
    }

    fun initView(){
        sb_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if(b)
                    EventBus.getDefault().post(SeekTo(i))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    fun clickListeners(){

        iv_play_pause.setOnClickListener {
            EventBus.getDefault().post(ToggleSong())
        }

        iv_next_song.setOnClickListener {
            EventBus.getDefault().post(PlayNextSong())
        }

        iv_back_song.setOnClickListener {
            EventBus.getDefault().post(PlayBackSong())
        }

        btn_back.setOnClickListener {
            (activity as MainActivity).mPager.setCurrentItem(0, true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChanged(event: SongChanged?) {
        if (event!!.isPlaying){
            Glide.with(context!!).load(R.drawable.btn_play_song).into(iv_play_pause)
        }else{
            Glide.with(context!!).load(R.drawable.btn_pause_song).into(iv_play_pause)
        }

        tv_song_name.text= event.songModel.Name
        sb_progress.max = event.duration
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChanged(event: UpdateTime?) {
        sb_progress.progress = event!!.progress
        tv_start_time.text = event.startTime
        tv_end_time.text = event.endTime
    }

    val MediaPlayer.seconds:String
        get() {
            var seconds1=this.duration / 1000
            var i=0
            while (seconds1>59){
                i += 1
                seconds1 -= 60
            }
            return if(seconds1<10){
                "$i:0$seconds1"
            }else{
                "$i:$seconds1"

            }
        }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}