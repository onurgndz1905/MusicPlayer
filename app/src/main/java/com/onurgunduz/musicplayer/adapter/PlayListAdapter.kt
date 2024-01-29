package com.onurgunduz.musicplayer.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onurgunduz.musicplayer.PlaylistActivity
import com.onurgunduz.musicplayer.PlaylistDetailsActivity
import com.onurgunduz.musicplayer.R
import com.onurgunduz.musicplayer.database.Playlist
import com.onurgunduz.musicplayer.databinding.PlaylistViewBinding

class PlayListAdapter(private val context: Context,
                      private var playList: ArrayList<Playlist>) :
    RecyclerView.Adapter<PlayListAdapter.MyHolder>() {

    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
        val delete = binding.playlistdelete

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = playList[position].name

        holder.name.isSelected = true

        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playList[position].name)

                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("YES") { dialog, _ ->
                    PlaylistActivity.musicPlayList.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }


        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetailsActivity::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context,intent,null)
        }
        if (PlaylistActivity.musicPlayList.ref[position].playlist.size >0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlayList.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.iconmusic).centerCrop())
                .into(holder.image)
        }

    }

    override fun getItemCount(): Int {
        return playList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        playList = ArrayList()
        playList.addAll(PlaylistActivity.musicPlayList.ref)
        notifyDataSetChanged()
    }
}