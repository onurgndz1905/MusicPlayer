package com.onurgunduz.musicplayer.workmanager

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.onurgunduz.musicplayer.R

class MyWorker(context: Context, params: WorkerParameters) : Worker(context, params){
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun getSystemService(notificationService: String): Any {
        TODO("Not yet implemented")
    }

    override fun doWork(): Result {
        // Bildirim oluştur
        val notification = NotificationCompat.Builder(applicationContext, "my_channel_id")
            .setContentTitle("Music Player")
            .setContentText("Muizik Dinleme zamanı geldi...")
            .setSmallIcon(R.drawable.musicmininicon)
            .build()

        // Bildirimi gönder

        notificationManager.notify(1, notification)

        return Result.success()
    }
}