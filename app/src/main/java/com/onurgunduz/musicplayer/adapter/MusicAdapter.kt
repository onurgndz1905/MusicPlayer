package com.onurgunduz.musicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.onurgunduz.musicplayer.PlayerActivity
import com.onurgunduz.musicplayer.PlaylistActivity
import com.onurgunduz.musicplayer.PlaylistDetailsActivity
import com.onurgunduz.musicplayer.R
import com.onurgunduz.musicplayer.database.Music
import com.onurgunduz.musicplayer.database.formatDuration
import com.onurgunduz.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList: ArrayList<Music>, private var playlistDetails : Boolean = false, private val selectionActivity : Boolean = false)
    : RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding : MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duraction = binding.songDuraction
        val root = binding.root
        val rootLayout = binding.rootLayout


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }



    override fun onBindViewHolder(holder: MyHolder, position: Int) {
       holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album

        holder.duraction.text = formatDuration(musicList[position].duration)
        holder.rootLayout.setOnClickListener {
            val intent =Intent(context, PlayerActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","MusicAdapter")
            ContextCompat.startActivity(context,intent,null)
        }
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
            .into(holder.image)

        when{
            playlistDetails->{
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if (addSong(musicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.blue))
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                }

            }
            else ->{
                holder.root.setOnClickListener {
                    when {
                        musicList[position].id == PlayerActivity.nowPlayingId -> {
                            sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                        }
                        else -> {
                            sendIntent(ref = "MusicAdapter", pos = position)
                        }
                    }

                }
            }
        }

    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id ==music.id){
                PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.add(song)
        return true
    }
    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlayList.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}