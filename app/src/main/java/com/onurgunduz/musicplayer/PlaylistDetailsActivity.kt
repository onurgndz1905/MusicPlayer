package com.onurgunduz.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.onurgunduz.musicplayer.adapter.MusicAdapter
import com.onurgunduz.musicplayer.database.MusicPlaylist
import com.onurgunduz.musicplayer.databinding.ActivityPlaylistBinding
import com.onurgunduz.musicplayer.databinding.ActivityPlaylistDetailsBinding

@Suppress("DEPRECATION")
class PlaylistDetailsActivity : AppCompatActivity() {

private lateinit var binding : ActivityPlaylistDetailsBinding
  private lateinit var adapter : MusicAdapter

    companion object{
        var currentPlaylistPos: Int = -1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos = intent.extras?.get("index") as Int
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)

        adapter = MusicAdapter(this,PlaylistActivity.musicPlayList.ref[currentPlaylistPos].playlist, playlistDetails = true)

        binding.playlistDetailsRV.adapter = adapter

        binding.backPlayList.setOnClickListener { finish() }

        binding.shuffleButtonPD.setOnClickListener {
            val intent = Intent(this,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","PlaylistDetailsShuffle")
            startActivity(intent)
        }

        binding.addSong.setOnClickListener {
            startActivity(Intent(this,SelectionActivity::class.java))
        }
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes"){dialog , _->
                    PlaylistActivity.musicPlayList.ref[currentPlaylistPos].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog,_->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.playlistNamePD.text = PlaylistActivity.musicPlayList.ref[currentPlaylistPos].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n "+
                    "Created On : ${PlaylistActivity.musicPlayList.ref[currentPlaylistPos].createdOn}\n\n"+
                    "Name :${PlaylistActivity.musicPlayList.ref[currentPlaylistPos].createBy}"

        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlayList.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
                .into(binding.playListImgPD)
            binding.shuffleButtonPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
        val editor = getSharedPreferences("FAVORİTES", MODE_PRIVATE).edit()

        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlayList)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}