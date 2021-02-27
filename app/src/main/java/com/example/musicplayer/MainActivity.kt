package com.example.musicplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.fragment.SongDetailFragment
import com.example.musicplayer.fragment.SongListFragment
import com.example.musicplayer.model.AudioModel
import com.example.musicplayer.utils.CustomAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CustomAppCompatActivity() {

    lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPager = findViewById(R.id.viewpager)
        if (isReadStoragePermissionGranted()){
            initView()
        }
        btn_grant_permission.setOnClickListener{
            isReadStoragePermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initView()
            }
        }
    }

    enum class fragments{
        list,detail
    }

    var currentFragment=fragments.list

    fun initView(){
        btn_grant_permission.visibility=View.GONE
        mAdapter = MyAdapter(this)
        mAdapter.addFragment(SongListFragment())
        mAdapter.addFragment(SongDetailFragment())
        mPager.adapter=mAdapter
        mPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentFragment=if (position==0) fragments.list else fragments.detail
            }
        })
    }

    override fun onBackPressed() {
        if (currentFragment==fragments.detail){
            mPager.setCurrentItem(0,true)
        }else{
            super.onBackPressed()
        }
    }

    fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("FragmentActivity", "Permission is granted1")
                true
            } else {
                Log.e("FragmentActivity", "Permission is revoked1")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.FOREGROUND_SERVICE), 3)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    class MyAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

        private val arrayList: ArrayList<Fragment> = ArrayList()

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun createFragment(position: Int): Fragment {
            return arrayList[position]
        }


        fun addFragment(fragment: Fragment?) {
            arrayList.add(fragment!!)
        }

    }

    lateinit var mPager: ViewPager2

    companion object {

    }

}

class SongChanged( var songModel:AudioModel,var isPlaying:Boolean,val duration:Int) { }
class PlaySongAt (val position: Int){ /* Additional fields if needed */ }
class PlayNextSong(){}
class PlayBackSong(){}
class ToggleSong(){}
class UpdateData(){}
class UpdateTime(val startTime:String,val endTime:String,val progress:Int){}
class SeekTo(val position: Int){}
