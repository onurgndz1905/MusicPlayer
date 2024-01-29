package com.onurgunduz.musicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.onurgunduz.musicplayer.adapter.MusicAdapter
import com.onurgunduz.musicplayer.database.Music
import com.onurgunduz.musicplayer.database.MusicPlaylist
import com.onurgunduz.musicplayer.database.exitApplication
import com.onurgunduz.musicplayer.databinding.ActivityMainBinding
import com.onurgunduz.musicplayer.workmanager.MyWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toogle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter


    companion object {
        lateinit var MusicListMA: ArrayList<Music>

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = "mychanel_id"
        val channelName = "My Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationChannel = NotificationChannel(channelId,channelName,importance)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        startWorkManager()
            initializeLayout()
            FavoriActivity.favoriteSong = ArrayList()
            val editor = getSharedPreferences("FAVORİTES", MODE_PRIVATE)
            val jsonString = editor.getString("FavoriteSongs",null)
            val typeToken = object :TypeToken<ArrayList<Music>>(){}.type
            if (jsonString != null){
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
                FavoriActivity.favoriteSong.addAll(data)
            }
        PlaylistActivity.musicPlayList = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist",null)
        if (jsonStringPlaylist != null){
            val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist,MusicPlaylist::class.java)
            PlaylistActivity.musicPlayList = dataPlaylist
        }





        binding.shufle.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)

            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")

            startActivity(intent)
        }
        binding.playlist.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }
        binding.favori.setOnClickListener {
            val intent = Intent(this@MainActivity, FavoriActivity::class.java)
            startActivity(intent)
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.navAbout -> {
                    val intent = Intent(this@MainActivity,AboutActivity::class.java)
                    intent.putExtra("index",0)
                    intent.putExtra("class","MainActivity")
                    startActivity(intent)
                }
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")

                        .setPositiveButton("yes") { _, _ ->
                            exitApplication()
                        }


                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)

                }

            }
            true
        }
    }
    @SuppressLint("InvalidPeriodicWorkRequestInterval")
    private fun startWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            2, // İşlem sıklığı (saat cinsinden)
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }


    private val PERMISSION_REQUEST_CODE = 13

    private fun requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            // Android 33 ve üstü için READ_MEDIA_AUDIO iznini kontrol et
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                // READ_MEDIA_AUDIO izni verilmişse, WRITE_EXTERNAL_STORAGE iznini kontrol et
            }
        } else {
            // Android sürümü 33'ten küçükse sadece WRITE_EXTERNAL_STORAGE iznini kontrol et
        }
    }





    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    // İzin verildi, gerekli işlemi burada yapabilirsiniz
                    refreshActivty()

                } else {
                    // İzin reddedildi, izni tekrar talep etmek için burayı kullanabilirsiniz
                    //  if (Manifest.permission == android.Manifest.permission.READ_MEDIA_AUDIO || permission == android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                      //  requestRuntimePermission()
                   // }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    private fun refreshActivty() {
        val intet = intent
        finish()
        startActivity(intet)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("SetTextI18n")
    private fun initializeLayout(){

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestRuntimePermission()


        toogle = ActionBarDrawerToggle(this,binding.root,binding.toolbar,R.string.open,R.string.close)
        toogle.isDrawerIndicatorEnabled = true
        binding.root.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val musicList = ArrayList<String>()
        CoroutineScope(Dispatchers.IO).launch {
            MusicListMA = getAllAudio()
            withContext(Dispatchers.Main) {
                binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
                musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
                binding.musicRV.adapter = musicAdapter
                binding.musiclistsize.text = "Total Songs : " +musicAdapter.itemCount
            }
        }
    }

    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllAudio() : ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",null)

        if (cursor != null){
            if (cursor.moveToFirst())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val duractionC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("context://media/external/audio/album")
                    val artUriC = Uri.withAppendedPath(uri,albumIdC).toString()
                    val music = Music(id=idC, title = titleC, album = albumC, artist = artistC, duration = duractionC, path = pathC, artUri = artUriC)
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)

                }while (cursor.moveToNext())
                cursor.close()
        }

        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService !=null){
        exitApplication()

        }

    }

    override fun onResume() {
        super.onResume()

        val editor = getSharedPreferences("FAVORİTES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavoriActivity.favoriteSong)
        editor.putString("FavoriteSongs",jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlayList)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }


    //search menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu,menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean = true

            override fun onQueryTextChange(p0: String?): Boolean {
              Toast.makeText(this@MainActivity,p0.toString(),Toast.LENGTH_SHORT).show()
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

}