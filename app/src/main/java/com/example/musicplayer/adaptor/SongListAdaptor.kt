package com.example.musicplayer.adaptor

import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import com.example.musicplayer.PlaySongAt
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioModel
import org.greenrobot.eventbus.EventBus

class SongListAdaptor: RecyclerView.Adapter<SongListAdaptor.ViewHolder>() {

    companion object{
       var SongList=ArrayList<AudioModel>()
    }

    var currentPlayingSongPath:String=""

    fun setList(list:ArrayList<AudioModel>){
        SongList=list
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view=LayoutInflater.from(p0.context).inflate(R.layout.rv_item_song,p0,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return SongList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.set(SongList[p1].Name,SongList[p1].Artist,SongList[p1].time,p1, SongList[p1].Path)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songName:TextView = itemView.findViewById(R.id.tv_song_name)
        var songArtist:TextView = itemView.findViewById(R.id.tv_artist_name)
        var songDuration:TextView = itemView.findViewById(R.id.tv_time)
        var main_container:View = itemView.findViewById(R.id.main_container)

        fun set(name:String,artist:String,duration:String,position:Int,path:String){
            songName.text=name
            songArtist.text=artist
            songDuration.text=duration

            if(path==currentPlayingSongPath){
                main_container.backgroundTintList=ColorStateList.valueOf(themeColor(R.attr.selectedColor))
            }else{
                main_container.backgroundTintList= ColorStateList.valueOf(themeColor(R.attr.cardBackgroundColor))
            }

            itemView.setOnClickListener {
                EventBus.getDefault().post(PlaySongAt(position,itemView.context))
            }
        }

        fun themeColor(@AttrRes attrRes: Int): Int {
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(attrRes, typedValue, true)
            return typedValue.data
        }

    }
}