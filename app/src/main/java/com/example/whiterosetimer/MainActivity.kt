package com.example.whiterosetimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.whiterosetimer.MainService
import com.example.whiterosetimer.MainService.LocalBinder
import java.util.*
import android.util.Log


class MainActivity : AppCompatActivity() {
    private lateinit var timer2 : Timer
    var mService: MainService? = null
    val mw = this
    var timer_update_locallog : Timer? = null
    val locallog = ArrayList<String>()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            Log.v("WhiteRoseActivity", "onServiceConnected")
            val binder = service as LocalBinder
            mService = binder.service

            // create timer
            timer_update_locallog = Timer()
            timer_update_locallog?.schedule(object : TimerTask() {
                override fun run() {
                    // start in UI

                    if (mService == null) return
                    val queue = mService?.get_queue()

                    // update local log
                    if (queue != null) {
                        while (queue.isNotEmpty()) {
                            val msg = queue.poll()
                            locallog.add(msg)
                        }
                    }

                    runOnUiThread {
                        // update label
                        val label = findViewById<TextView>(R.id.locallog)
                        var text = ""
                        for (i in locallog!!.indices) {
                            text += locallog[i] + "\n"
                            label.text = text
                        }
                    }
                }
            }, 0, 1000)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.v("WhiteRoseActivity", "onServiceDisconnected")}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // start service
        val intent = Intent(this, MainService::class.java)
        startForegroundService(intent)

        // create service connection
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        // count of milliseconds to the next second
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