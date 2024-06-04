package com.example.task2_secondspace

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimeCountService : Service() {

    private var foregroundElapsedTime : Long = 0
    private var backgroundElapsedTime : Long = 0
    private var lastMeasuredTime : Long = 0
    private var appState : AppState = AppState.BACKGROUND

    private val binder = object : TimeCountInterface.Stub() {
        override fun countTime(){
                val currentTime = SystemClock.elapsedRealtime()
                if (appState == AppState.BACKGROUND) {
                    backgroundElapsedTime += currentTime - lastMeasuredTime
                } else {
                    foregroundElapsedTime += currentTime - lastMeasuredTime
                }
                sendUpdateBroadcast()
                lastMeasuredTime = currentTime
        }

        override fun getBackgroundTime () : String
        {
            return getFormattedTime(backgroundElapsedTime)
        }

        override fun getForegroundTime () : String
        {
            return getFormattedTime(foregroundElapsedTime)
        }

        override fun setAppInForeground(){
            forceUpdateInBetween()
            appState = AppState.FOREGROUND
        }

        override fun setAppInBackground(){
            forceUpdateInBetween()
            appState = AppState.BACKGROUND

        }

    }


    override fun onCreate() {
        super.onCreate()
        lastMeasuredTime = SystemClock.elapsedRealtime()
    }

    private fun sendUpdateBroadcast()
    {
        val intent = Intent("com.example.app.UPDATE_TEXT").apply{
            putExtra("ForegroundTime", getFormattedTime(foregroundElapsedTime))
            putExtra("BackgroundTime", getFormattedTime(backgroundElapsedTime))
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun forceUpdateInBetween()
    {
        val currentTime = SystemClock.elapsedRealtime()
        if (this.appState == AppState.BACKGROUND) {
            this.backgroundElapsedTime += currentTime - this.lastMeasuredTime
        } else {
            this.foregroundElapsedTime += currentTime - this.lastMeasuredTime
        }
        this.lastMeasuredTime = currentTime
    }

    //Receiving time in 100ms. Converting to chronometric time.
    private fun getFormattedTime (ms : Long) : String
    {
        var millis = ms
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        val millisTruncated = (String.format("$ms")).takeLast(3)

        return "${if (minutes < 10) "0" else ""} $minutes:" +
                "${if (seconds < 10) "0" else ""} $seconds." +
                millisTruncated
    }
}

enum class AppState{
    FOREGROUND,
    BACKGROUND
}