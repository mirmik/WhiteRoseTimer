package com.example.whiterosetimer

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import com.example.whiterosetimer.MainService
import android.content.Intent


class MainActivity : AppCompatActivity() {
    private lateinit var timer2 : Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // start service
        val intent = Intent(this, MainService::class.java)
        startService(intent)

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        // count of milliseconds to the next second
        val millis_to_next_second = 1000 - millis
        Log.v("MyActivity", millis_to_next_second.toString())

        // count of milliseconds to the next minute
        val millis_to_next_minute = 60000 - second * 1000 - millis
        
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