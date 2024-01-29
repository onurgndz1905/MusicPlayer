package com.onurgunduz.musicplayer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import com.onurgunduz.musicplayer.MainActivity
import com.onurgunduz.musicplayer.NotificationReceiver
import com.onurgunduz.musicplayer.PlayerActivity
import com.onurgunduz.musicplayer.R
import com.onurgunduz.musicplayer.database.formatDuration


class MusicService : Service()  {
    // Müzik servisi sınıfımız, bir Android Service sınıfından türetilmiştir.

    private var myBinder = MyBinder()
    // Bir özel Binder sınıfı olan myBinder, bu servisin müziği kontrol etmek için kullanılabilir.

    var mediaPlayer: MediaPlayer? = null
    // mediaPlayer, şu anda çalınan müziği temsil eden bir MediaPlayer nesnesini saklar.

   private lateinit var mediaSession : MediaSessionCompat
   private lateinit var runnable: Runnable

    override fun onBind(p0: Intent?): IBinder {
        // Service sınıfının onBind() yöntemi, bu servis bir istemciye bağlandığında çağrılır.
        // Bu yöntem, myBinder nesnesini döndürerek istemcinin bu servisi kullanabilmesini sağlar.
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        // MyBinder sınıfı, bu servise erişim sağlayan bir iç sınıftır.

        fun currentService(): MusicService {
            // currentService() yöntemi, bu servis örneğini döndürür.
            // Bu, istemcilerin bu servise erişim sağlamasına olanak tanır.
            return this@MusicService
        }
    }
    fun showNotifaction(playaPauesBtn:Int){
        val intent = Intent(baseContext,MainActivity::class.java)
        intent.putExtra("index",PlayerActivity.songPosition)
        intent.putExtra("class","NowPlaying")
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


            val prevIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
            val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)


            val playIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
            val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

            val nextIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
            val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

            val exitIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
            val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE)



        val notification = androidx.core.app.NotificationCompat.Builder(baseContext,ApplicationClass.CHANEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.baseline_library_music_24)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.iconmusic))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(com.google.android.material.R.drawable.material_ic_keyboard_arrow_previous_black_24dp,"previous",prevPendingIntent)
            .addAction(playaPauesBtn ,"play",playPendingIntent)
            .addAction(R.drawable.baseline_navigate_next_24,"next",nextPendingIntent)
            .addAction(R.drawable.baseline_exit_to_app_24,"exit",exitPendingIntent)
            .build()

        startForeground(13,notification)
    }
     fun createMediaPlayer(){
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()

            PlayerActivity.binding.playpausebutton.setIconResource(R.drawable.baseline_pause_24)
            PlayerActivity.musicService!!.showNotifaction(R.drawable.baseline_pause_24)

            PlayerActivity.binding.seekBarStart.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarEnd.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())

            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration

            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id


        }catch (e: Exception){return}

    }
    fun sekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.seekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)

        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
}
