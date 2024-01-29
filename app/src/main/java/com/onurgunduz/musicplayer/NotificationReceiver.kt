package com.onurgunduz.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.onurgunduz.musicplayer.database.exitApplication
import com.onurgunduz.musicplayer.database.setSongPosition
import com.onurgunduz.musicplayer.service.ApplicationClass

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS ->prevNextSong(increment = false, context = context!!)

            ApplicationClass.NEXT ->prevNextSong(increment = true,context = context!!)


            ApplicationClass.PLAY ->{
                if (PlayerActivity.isPlaying) pauesMusic() else playMusic()
            }
            ApplicationClass.EXIT -> {
                    exitApplication()

                    }
                }
             }

    private fun playMusic(){

        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotifaction(R.drawable.baseline_pause_24)
        PlayerActivity.binding.playpausebutton.setIconResource(R.drawable.baseline_pause_24)
        NowPlaying.binding.playpauestBtnNP.setIconResource(R.drawable.baseline_pause_24)
    }

    private fun pauesMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotifaction(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playpausebutton.setIconResource(R.drawable.baseline_play_arrow_24)
        NowPlaying.binding.playpauestBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
    }
    private fun prevNextSong(increment:Boolean,context: Context){
        setSongPosition(increment = increment)

        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
            .into(PlayerActivity.binding.SongImgPA)
        PlayerActivity.binding.songNamepa.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
            .into(NowPlaying.binding.songImgNP)
       NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        if (PlayerActivity.isFavourite) PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
        else PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)


    }


}