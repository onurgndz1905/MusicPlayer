package com.onurgunduz.musicplayer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.onurgunduz.musicplayer.workmanager.MyWorker
import java.util.concurrent.TimeUnit


class BackGroundWorkManager:Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val peridoicWorkRequest = PeriodicWorkRequest.Builder(
            MyWorker::class.java,2,TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(peridoicWorkRequest)
        return START_STICKY

    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}