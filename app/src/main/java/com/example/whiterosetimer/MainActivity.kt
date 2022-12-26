package com.example.whiterosetimer

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.whiterosetimer.WhiteRoseService.LocalBinder
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var timer2 : Timer
    var mService: WhiteRoseService? = null
    val mw = this
    var timer_update_locallog : Timer? = null

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            Log.v("WhiteRoseActivity", "onServiceConnected")
            val binder = service as LocalBinder
            mService = binder.service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.v("WhiteRoseActivity", "onServiceDisconnected")}
    }

    fun stop_service() {
        // stop foreground service
        //return

        if (mService != null) {
            mService?.pre_stop_service()
            mService = null
            val intent = Intent(this, WhiteRoseService::class.java)
            stopService(intent)
            unbindService(serviceConnection)
        }

        runOnUiThread {
            val label = findViewById<TextView>(R.id.locallog)
            label.text = "Service stopped"
        }
    }

    fun start_service()
    {
        if (mService != null)
            return    

        // start service
        val intent = Intent(this, WhiteRoseService::class.java)
        startForegroundService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        

        runOnUiThread {
            val label = findViewById<TextView>(R.id.locallog)
            label.text = "Service started"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val label = findViewById<TextView>(R.id.locallog)
        label.movementMethod = ScrollingMovementMethod()

        start_service()
        start_clock_timer()

        // get button
        val stop_button = findViewById<TextView>(R.id.button_stop)
        stop_button.setOnClickListener {
            if (mService != null) {
                stop_service()
                stop_button.text = "start service"
            }
            else {
                start_service()
                stop_button.text = "stop service"
            }
        }

        val journal_button = findViewById<TextView>(R.id.btn_journal)
        journal_button.setOnClickListener {
            if (mService != null) {

                val locallog = mService?.get_journal()

                runOnUiThread {
                    val label = findViewById<TextView>(R.id.locallog)
                    label.text = ""
                    for (s in locallog!!)
                        label.append(s + "\n")
                }
            }
        }
    }

    fun start_clock_timer() {
        val calendar = Calendar.getInstance()
        val millis = calendar.get(Calendar.MILLISECOND)
        val millis_to_next_second = 1000 - millis

        timer2 = Timer()
        timer2.schedule(object : TimerTask() {
            override fun run() {
                // update label
                val label = findViewById<TextView>(R.id.seconds)
                val calendar = Calendar.getInstance()
                val second = calendar.get(Calendar.SECOND)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val text = hour.toString() + ":" + minute.toString() + ":" + second.toString()

                // update label in UI thread
                runOnUiThread {
                    label.text = text
                }
            }
        }, millis_to_next_second.toLong(), 1000)
    }
}