package com.example.task2_secondspace

import java.util.concurrent.TimeUnit
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.task2_secondspace.R.*


var ElapsedTimeForeground = 0
var ElapsedTimeBackground = 0

var AppInForeground : Boolean = false



class MainActivity : AppCompatActivity() {

    //Text Views
    lateinit var timePassedB: TextView
    lateinit var timePassedF: TextView

    //Event handlers
    var mHandlerF : Handler? = null
    var mHandlerB : Handler? = null


    //Starting cyclic alarm for foreground and stopping background
    private fun startFTimer()
    {
        mHandlerB?.removeCallbacks(chronometerB)
        mHandlerF = Handler(Looper.getMainLooper())
        chronometerF.run()
    }

    //Starting cyclic alarm for background and stopping foreground
    private fun startBTimer()
    {
        mHandlerF?.removeCallbacks(chronometerF)
        mHandlerB = Handler(Looper.getMainLooper())
        chronometerB.run()
    }

    //Receiving time in 100ms. Converting to chronometric time.
    private fun getFormattedTime (@Suppress("SpellCheckingInspection") hundredms : Int) : String
    {
        var millis = hundredms * 100L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        val millisTruncated = (String.format("$hundredms")).takeLast(1)

        return "${if (minutes < 10) "0" else ""} $minutes:" +
                "${if (seconds < 10) "0" else ""} $seconds." +
                millisTruncated
    }

    //Cyclic runnable for foreground time
    private var chronometerF : Runnable = object : Runnable {
        override fun run() {
            try {
                if(AppInForeground) {
                    ElapsedTimeForeground++
                    timePassedF.text = getFormattedTime(ElapsedTimeForeground)
                }
            } finally {
                mHandlerF!!.postDelayed(this, 100L)
            }
        }
    }

    //Cyclic runnable for background time
    private var chronometerB : Runnable = object : Runnable {
        override fun run() {
            try {
                if(!AppInForeground) {
                    ElapsedTimeBackground++
                    timePassedB.text = getFormattedTime(ElapsedTimeBackground)
                }
            } finally {
                mHandlerB!!.postDelayed(this, 100L)
            }
        }
    }
    
    //On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContentView(layout.activity_main)

        timePassedB = findViewById(id.TimeNotOpened)
        timePassedF = findViewById(id.TimeOpened)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //On resume (foreground)
    override fun onResume()
    {
        super.onResume()
        AppInForeground = true
        startFTimer()
    }

    //On pause (background)
    override fun onPause()
    {
        super.onPause()
        AppInForeground = false
        startBTimer()
    }
}