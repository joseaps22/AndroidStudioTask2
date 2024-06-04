package com.example.task2_secondspace

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.task2_secondspace.R.id
import com.example.task2_secondspace.R.layout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    //Text Views
    private lateinit var timePassedB: TextView
    private lateinit var timePassedF: TextView
    private var appTimeCountHandler : TimeCountInterface? = null
    private var isBound = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    private val timePassedReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            timePassedF.text = intent?.getStringExtra("ForegroundTime")
            timePassedB.text = intent?.getStringExtra("BackgroundTime")
        }
    }

    private fun startUpdatingTextViews(){
        coroutineScope.launch {

            while (isBound)
            {
                appTimeCountHandler?.countTime()
                delay(100)
            }
        }
    }


    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBound = true
            appTimeCountHandler = TimeCountInterface.Stub.asInterface(service)
            appTimeCountHandler?.setAppInForeground()
            startUpdatingTextViews()
            Log.d("MainActivity", "Service connected")
            Log.d("MainActivity", "$isBound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false

        }
    }
    
    //On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContentView(layout.activity_main)

        timePassedB = findViewById(id.TimeNotOpened)
        timePassedF = findViewById(id.TimeOpened)

        registerReceiver(timePassedReceiver, IntentFilter("com.example.app.UPDATE_TEXT"))

        val intent = Intent(this, TimeCountService::class.java)
        startService(intent)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE )


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
        appTimeCountHandler?.setAppInForeground()
    }

    //On pause (background)
    override fun onPause()
    {
        super.onPause()
        appTimeCountHandler?.setAppInBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isBound)
        {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}