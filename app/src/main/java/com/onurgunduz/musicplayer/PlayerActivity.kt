package com.onurgunduz.musicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.DynamicsProcessing.Eq
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onurgunduz.musicplayer.database.Music
import com.onurgunduz.musicplayer.database.exitApplication
import com.onurgunduz.musicplayer.database.favouriteChecker
import com.onurgunduz.musicplayer.database.formatDuration
import com.onurgunduz.musicplayer.database.setSongPosition
import com.onurgunduz.musicplayer.databinding.ActivityPlayerBinding
import com.onurgunduz.musicplayer.service.MusicService
import java.lang.Exception
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() ,ServiceConnection, MediaPlayer.OnCompletionListener{


    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition : Int = 0
        var isPlaying : Boolean = false
        var musicService : MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPlayerBinding

        var repeat : Boolean = false

        var min15 : Boolean = false
        var min30 : Boolean = false
        var min60 : Boolean = false
        var nowPlayingId : String = ""
        var isFavourite : Boolean = false
        var fIndex : Int = -1




    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeLayout()

        binding.playpausebutton.setOnClickListener {
        if (isPlaying)paueseMusic()
            else playMusic()
        }
        binding.nextMusic.setOnClickListener { prevNextSong(increment = false) }
        binding.backmusic.setOnClickListener { prevNextSong(increment = true) }
        binding.backBtnPA.setOnClickListener { finish() }

        binding.seekBarPA.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
            }
            else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.blue)
                )
            }
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val EqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                EqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                EqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                EqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(EqIntent,13)
            }catch (e:Exception){Toast.makeText(this,"Equalizer Feature not Supported!!",Toast.LENGTH_SHORT).show() }
        }
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) showBottomSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer ")
                    .setMessage("Do you want to stop timer?")

                    .setPositiveButton("YES"){_,_ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.blue))
                    }


                    .setNegativeButton("No"){dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            }
        }
        binding.shareBtnPA.setOnClickListener {
            val sharIntent = Intent()
            sharIntent.action = Intent.ACTION_SEND
            sharIntent.type = "audio/*"
            sharIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(sharIntent,"Sharing Music File!!"))

        }
        binding.favoriteBtnPA.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)
                FavoriActivity.favoriteSong.removeAt(fIndex)
            }
            else{
                isFavourite = true
                binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
                FavoriActivity.favoriteSong.add(musicListPA[songPosition])
            }
        }

    }
    private fun setLayot(){
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
            .into(binding.SongImgPA)
        binding.songNamepa.text = musicListPA[songPosition].title

        if (repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
        if (min15 || min30 || min60) binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
        if (isFavourite) binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_24)
        else binding.favoriteBtnPA.setImageResource(R.drawable.baseline_favorite_border_24)


    }
    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playpausebutton.setIconResource(R.drawable.baseline_pause_24)
            musicService!!.showNotifaction(R.drawable.baseline_pause_24)

            binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
        }catch (e:Exception){return}

    }
    private fun initializeLayout(){
        songPosition= intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "FavoriteAdapter" ->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavoriActivity.favoriteSong)
                setLayot()
            }
            "NowPlaying" ->{
                setLayot()
                binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playpausebutton.setIconResource(R.drawable.baseline_pause_24)
                else binding.playpausebutton.setIconResource(R.drawable.baseline_play_arrow_24)
            }
            "MusicAdapter" ->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)


                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayot()


            }
            "MainActivity" ->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayot()

            }
            "FavoriteShuffle" ->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(FavoriActivity.favoriteSong)
                musicListPA.shuffle()
                setLayot()
            }
            "PlaylistDetailsAdapter"->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)

                setLayot()
            }
            "PlaylistDetailsShuffle" ->{
                //service starting ( servisi başlatalıyor )
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)
                musicListPA.shuffle()

                setLayot()
            }

        }
    }
    private fun playMusic(){
        binding.playpausebutton.setIconResource(R.drawable.baseline_pause_24)
        musicService!!.showNotifaction(R.drawable.baseline_pause_24)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }
    private fun paueseMusic(){
        binding.playpausebutton.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotifaction(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment:Boolean){
        if (increment)
        {
            setSongPosition(increment = true)
            setLayot()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment=false)
            setLayot()
            createMediaPlayer()
        }
    }


    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.sekBarSetup()


    }

    override fun onServiceDisconnected(p0: ComponentName?) {
       musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {

        setSongPosition(increment = true)
        createMediaPlayer()
        try {setLayot()}catch (e:Exception){return}

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 15 minutes",Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
            min15 = true
            Thread{Thread.sleep((15*60000).toLong())
            if (min15) exitApplication() }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min30)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 30 minutes",Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
            min30 = true
            Thread{Thread.sleep((30*60000).toLong())
                if (min30) exitApplication() }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 60 minutes",Toast.LENGTH_SHORT).show()

            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
            min60 = true
            Thread{Thread.sleep((60*60000).toLong())
                if (min60) exitApplication() }.start()
            dialog.dismiss()
        }
    }

}