package com.onurgunduz.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onurgunduz.musicplayer.adapter.PlayListAdapter
import com.onurgunduz.musicplayer.database.MusicPlaylist
import com.onurgunduz.musicplayer.database.Playlist
import com.onurgunduz.musicplayer.databinding.ActivityPlaylistBinding
import com.onurgunduz.musicplayer.databinding.AddPlaylistDialogBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPlaylistBinding
    private lateinit var adapter : PlayListAdapter

    companion object{
        var musicPlayList : MusicPlaylist = MusicPlaylist()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity,2)
        adapter = PlayListAdapter(this, playList = musicPlayList.ref)
        binding.playlistRV.adapter = adapter
        binding.backPlayList.setOnClickListener { finish() }
        binding.addplaylist.setOnClickListener { customAlertDialog() }


    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog,binding.root,false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD"){dialog,_->
                val playlistName = binder.playlistName.text
                val createdBy = binder.playYourName.text
                if (playlistName !=null && createdBy !=null)
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addplaylist(playlistName.toString(),createdBy.toString())
                    }
                dialog.dismiss()
            }.show()
    }

    private fun addplaylist(name:String , createdBy:String) {
        var playlisExists = false
        for (i in musicPlayList.ref){
            if (name.equals(i.name)){
                playlisExists = true
                break
            }
        }
        if (playlisExists) Toast.makeText(this,"Playlist Exist",Toast.LENGTH_SHORT).show()
        else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createBy = createdBy
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlayList.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}

