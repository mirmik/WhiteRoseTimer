package com.example.whiterosetimer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*
import android.app.PendingIntent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


class MainService : Service() , TextToSpeech.OnInitListener
{
    var mode : Int = 0

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
    private val timer : Timer = Timer()

    override fun onCreate() {
        Log.v("MyActivity", "onCreate")
        super.onCreate()
        tts = TextToSpeech(this, this)
        start_oneshoot_timer()
    }

    fun start_oneshoot_timer() 
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        // count of milliseconds to the next minute
        val millis_to_next_minute = 60000 - second * 1000 - millis
        Log.v("WhiteRoseTimer", "[$hour:$minute:$second] Start oneshoot timer for $millis_to_next_minute")

        timer.schedule(object : TimerTask() {
            override fun run() {
                speak_time()
                start_oneshoot_timer()
            }
        }, millis_to_next_minute.toLong())
    }
    
    fun number_to_text(number: Int): String {
        if (number in 0..20) {
            return numbers[number].toString()
        } else if (number in 21..59) {
            if (number % 10 == 0)
                return numbers[number].toString()
            else
                return numbers[number - number % 10] + " " + numbers[number % 10]
        } else {
            return "error"
        }
    }

    class LocalBinder(val service: MainService) : Binder() {}

    private val mBinder: IBinder = LocalBinder(this)
    override fun onBind(intent: Intent) : IBinder?
    {
        Log.v("MyActivity", "onBind")
        return null
    }

    private fun speak(text: String)
    {
        tts.speak("k", TextToSpeech.QUEUE_ADD, null, null)
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    private fun time_to_speak_hour(hour : Int, minute : Int) : Boolean
    {
        //val checkbox1_state = findViewById<RadioButton>(R.id.radioButton1).isChecked
        //val checkbox2_state = findViewById<RadioButton>(R.id.radioButton2).isChecked
        //val checkbox3_state = findViewById<RadioButton>(R.id.radioButton3).isChecked
        //val checkboxS_state = findViewById<RadioButton>(R.id.radioButtonSpecial).isChecked

        if (mode == 0)
            return true;

        if (mode == 1) {
            return when (minute) {
                0, 30 -> true
                else -> false
            }
        }

        if (mode == 2) {
            return when (minute) {
                0, 15, 30, 45 -> true
                else -> false
            }
        }

        if (mode == 3) {
            return when (minute) {
                0, 10, 20, 30, 40, 50 -> true
                else -> false
            }
        }

        return false
    }

    private fun time_to_speak_minute(hour : Int, minute : Int) : Boolean
    {
        return !time_to_speak_hour(hour, minute)
    }

    private fun speak_time()
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        //val checkboxS_state = findViewById<RadioButton>(R.id.radioButtonSpecial).isChecked
        if (mode == 0)
        {
            val time = number_to_text(minute) + " " + number_to_text(hour)
            speak(time)
            return
        }

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

    override fun onStartCommand(intent:Intent , flags : Int, startId : Int) : Int {
        Log.v("MyActivity", "onStartCommand")

        val CHANNEL_ID = "10043"

        val notificationManager : NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel : NotificationChannel = NotificationChannel(CHANNEL_ID, "My channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent =
            Intent(this, MainService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("notification_title")
            .setContentText("content_text")
            //.setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
            .setTicker("ticker_text")
            .build()

// Notification ID cannot be 0.
        startForeground(10042, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        Log.v("MyActivity", "onDestroy")
        super.onDestroy()
    }
}
