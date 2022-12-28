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
import android.graphics.Color
import android.widget.SeekBar


class MainActivity : AppCompatActivity() {
    private lateinit var timer2 : Timer
    var mService: WhiteRoseService? = null

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

    fun button_set_start_state(b : Boolean) {
        val stop_button = findViewById<TextView>(R.id.button_stop)
        if (b) {
            stop_button.text = "start service"
            stop_button.setBackgroundColor(Color.GREEN)    
        }
        else
        {
            stop_button.text = "stop service"
            stop_button.setBackgroundColor(Color.RED)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val label = findViewById<TextView>(R.id.locallog)
        label.movementMethod = ScrollingMovementMethod()

        start_service()
        button_set_start_state(false)
        start_clock_timer()

        // get button
        val stop_button = findViewById<TextView>(R.id.button_stop)
        stop_button.setOnClickListener {
            if (mService != null) {
                stop_service()
                button_set_start_state(true)
            }
            else {
                start_service()
                button_set_start_state(false)
            }
        }


        val btn_test = findViewById<TextView>(R.id.btn_test)
        btn_test.setOnClickListener {
                mService?.speak_time()
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

        // get volume bar 
        val volume_bar = findViewById<SeekBar>(R.id.volume_bar)
        volume_bar.setMax(100)
        volume_bar.setProgress(100)
        volume_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mService != null) {
                    mService?.set_volume(progress / 100.0f)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
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