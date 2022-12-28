package com.example.whiterosetimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*
import android.app.AlarmManager
import android.content.BroadcastReceiver
import  android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.IntentFilter
import android.os.Bundle
import android.media.AudioManager


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

class WhiteRoseService : Service() , TextToSpeech.OnInitListener
{
    var mode : Int = 0
    val logjournal = mutableListOf<String>()
    var tthis = this
    var started = false
    var volume = 0.5f

    // receive messages from AlarmReceiver
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.v("WhiteRoseTimer", "Receive Message from timer+")
            tthis.do_action()
        }
    }

    private lateinit var tts : TextToSpeech
    //private val timer : Timer = Timer()

    fun get_journal() : List<String>
    {
        return logjournal
    }

    fun set_volume(volume: Float)
    {
        Log.v("WhiteRoseTimer", "setVolume " + volume.toString())
        this.volume = volume
    }

    override fun onCreate() {
        Log.v("WhiteRoseTimer", "onCreate")
        super.onCreate()
        tts = TextToSpeech(this, this)

        val filter = IntentFilter()
        filter.addAction("whiterosetimer.timeToSpeak")
        registerReceiver(receiver, filter)
    }

    fun do_action()
    {
        speak_time()
        start_exact_alarm_for_next_minute()
    }

    fun start_exact_alarm_for_next_minute()
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val millis_to_next_minute = 60000 - second * 1000 - millis
        log_time(millis_to_next_minute, hour, minute, second)

        val intent = Intent("whiterosetimer.timeToSpeak")
        val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_IMMUTABLE )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millis_to_next_minute, pendingIntent)
    }

    fun log_time(ms : Int, hour:Int, minute:Int, second:Int)
    {
        val msg = "[$hour:$minute:$second] Millis to next minute: $ms"
        logjournal.add(msg)
        Log.v(
            "WhiteRoseTimer",
            msg
        )
    
    }

    fun time_to_next_minute() : Long {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val millis_to_next_minute = 60000 - second * 1000 - millis
        log_time(millis_to_next_minute, hour, minute, second)
        return millis_to_next_minute.toLong()
    }

    class LocalBinder(val service: WhiteRoseService) : Binder() {}

    private val mBinder: IBinder = LocalBinder(this)
    override fun onBind(intent: Intent) : IBinder?
    {
        val extras = intent.extras
        return mBinder
    }

    private fun speak(text: String)
    {
        // option volume as Bundle
        val params = Bundle()
        params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION)
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)

        tts.speak("k", TextToSpeech.QUEUE_ADD, params, null)
        tts.speak(text, TextToSpeech.QUEUE_ADD, params, null)
    }

    fun pre_stop_service()
    {
        tts.stop()
        tts.shutdown()
        // receiver is unregistered in onDestroy
    }

    private fun time_to_speak_hour(hour : Int, minute : Int) : Boolean
    {
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

    fun speak_time()
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
        Log.v("WhiteRoseTimer", "onInit")
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.UK)
        } else {
            Log.e("WhiteRoseTimer", "Failed to Initialize")
        }

        for (voice in tts.voices)
        {
            Log.v("WhiteRoseTimer", voice.name)
            if (voice.name == "en-US-SMTf00")
                tts.voice = voice
        }
    }

    override fun onStartCommand(intent:Intent , flags : Int, startId : Int) : Int {
        Log.v("WhiteRoseTimer", "onStartCommand")

        val CHANNEL_ID = "10043"

        if (started)
            return START_STICKY

        val notificationManager : NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel : NotificationChannel = NotificationChannel(CHANNEL_ID, "My channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent =
            Intent(this, WhiteRoseService::class.java).let { notificationIntent ->
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

        started = true
        start_exact_alarm_for_next_minute()

        return START_STICKY
    }

    override fun onDestroy() {
        Log.v("WhiteRoseTimer", "OnDestroy")
        super.onDestroy()
        started = false
        unregisterReceiver(receiver)
    }

}
