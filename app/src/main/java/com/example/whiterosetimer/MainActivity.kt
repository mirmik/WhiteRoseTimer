package com.example.whiterosetimer

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener  {
    // map of numbers to words
    private val numbers: Map <Int, String> = hashMapOf(
        0 to "zero",
        1 to "one", 
        2 to "two", 
        3 to "three", 
        4 to "four", 
        5 to "five", 
        6 to "six", 
        7 to "seven", 
        8 to "eight", 
        9 to "nine", 
        10 to "ten", 
        11 to "eleven", 
        12 to "twelve", 
        13 to "thirteen", 
        14 to "fourteen", 
        15 to "fifteen", 
        16 to "sixteen", 
        17 to "seventeen", 
        18 to "eighteen", 
        19 to "nineteen", 
        20 to "twenty",
        30 to "thirty",
        40 to "forty",
        50 to "fifty")

    private lateinit var tts : TextToSpeech
    private lateinit var timer : Timer
    private lateinit var timer2 : Timer

    fun number_to_text(number: Int): String {
        if (number in 1..20) {
            return numbers[number].toString()
        } else if (number in 21..59) {
            return numbers[number - number % 10] + " " + numbers[number % 10]
        } else {
            return "error"
        }
    }

    private fun speak(text: String)
    {
        tts.speak("k", TextToSpeech.QUEUE_ADD, null, null)
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    private fun time_to_speak_hour(hour : Int, minute : Int) : Boolean
    {
        val checkbox1_state = findViewById<RadioButton>(R.id.radioButton1).isChecked
        val checkbox2_state = findViewById<RadioButton>(R.id.radioButton2).isChecked
        val checkbox3_state = findViewById<RadioButton>(R.id.radioButton3).isChecked

        if (checkbox1_state) {
            return when (minute) {
                0, 30 -> true
                else -> false
            }
        }

        if (checkbox2_state) {
            return when (minute) {
                0, 15, 30, 45 -> true
                else -> false
            }
        }

        if (checkbox3_state) {
            return when (minute) {
                0, 10, 20, 30, 40, 50 -> true
                else -> false
            }
        }

        return false
    }

    private  fun time_to_speak_minute(hour : Int, minute : Int) : Boolean
    {
        return !time_to_speak_hour(hour, minute)
    }

    private fun speak_time()
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (time_to_speak_minute(hour, minute))
        {
            val time = number_to_text(minute)
            speak(time)
        }

        if (time_to_speak_hour(hour, minute))
        {
            val time = number_to_text(hour) + " hour " + number_to_text(minute) + " minutes"
            speak(time)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)
        supportActionBar?.hide()

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        // count of milliseconds to the next second
        val millis_to_next_second = 1000 - millis
        Log.v("MyActivity", millis_to_next_second.toString())

        // count of milliseconds to the next minute
        val millis_to_next_minute = 60000 - second * 1000 - millis

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                speak_time()
            }
        }, millis_to_next_minute.toLong(), 60000)
        
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

        /*val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 200 as Int * 2,
            AudioTrack.MODE_STATIC
        )*/
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.UK)
        } else {
            Log.e("error", "Failed to Initialize")
        }

        for (voice in tts.voices)
        {
            Log.v("MyActivity", voice.name)
            if (voice.name == "en-US-SMTf00")
                tts.voice = voice
        }
    }
}