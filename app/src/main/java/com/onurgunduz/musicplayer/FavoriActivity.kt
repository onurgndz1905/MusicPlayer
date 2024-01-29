package com.onurgunduz.musicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.onurgunduz.musicplayer.adapter.FavoriteAdapter
import com.onurgunduz.musicplayer.database.Music
import com.onurgunduz.musicplayer.databinding.ActivityFavoriBinding

class FavoriActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFavoriBinding

    companion object {
        var favoriteSong: ArrayList<Music> = ArrayList()

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityFavoriBinding.inflate(layoutInflater)

        setContentView(binding.root)


        with(binding){
            backBtnFA.setOnClickListener { finish() }
            favoriteRV.setHasFixedSize(true)
            favoriteRV.setItemViewCacheSize(13)
            favoriteRV.layoutManager = GridLayoutManager(this@FavoriActivity,4)
            val adapter = FavoriteAdapter(this@FavoriActivity, favoriteSong)
            favoriteRV.adapter = adapter
            if (favoriteSong.size < 1) shuffleButton.visibility = View.INVISIBLE
            binding.shuffleButton.setOnClickListener {
                val intent = Intent(this@FavoriActivity, PlayerActivity::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "FavoriteShuffle")
                startActivity(intent)
            }
        }



    }


}