package com.example.task3

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var timeAfterBoot: TextView
    private var bootTime : MyAidlInterface? = null
    private var isBound = false
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

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

    private fun startUpdatingTimeCount(){
        coroutineScope.launch {
            while(isBound)
            {
                bootTime?.let {
                    val currentCount = it.countTime()
                    timeAfterBoot.text= getFormattedTime(currentCount)
                }
                delay(100)
            }
        }
    }


    private val timeCountConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bootTime = MyAidlInterface.Stub.asInterface(service)
            isBound = true
            startUpdatingTimeCount()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        timeAfterBoot = findViewById(R.id.timeCount)
        val intent = Intent(this, BootServiceHandler::class.java)
        bindService(intent, timeCountConnection, Context.BIND_AUTO_CREATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isBound){
            unbindService(timeCountConnection)
            isBound = false
        }
        coroutineScope.cancel()
    }

}
