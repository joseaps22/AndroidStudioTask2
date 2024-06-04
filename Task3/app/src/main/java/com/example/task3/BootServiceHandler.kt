package com.example.task3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class BootServiceHandler : Service() {

    private var bootTime: Long = 0
    private val binder = object : MyAidlInterface.Stub() {
        override fun countTime(): Long {
            return SystemClock.elapsedRealtime() - bootTime
        }
    }

    override fun onCreate() {
        super.onCreate()
        bootTime = SystemClock.elapsedRealtime()
        startForegroundService()
    }

    private fun startForegroundService() {
        val channelId = "BootServiceChannel"
        val channelName = "Boot Service Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Boot Service")
            .setContentText("Counting time since boot...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}