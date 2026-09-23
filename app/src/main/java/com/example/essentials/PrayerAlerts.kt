package com.example.essentials

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.media.*
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import org.json.JSONArray
import java.util.Locale

object PrayerAlerts {
    private fun pending(context: Context, id: Int, name: String = "", exact: Boolean = false): PendingIntent = PendingIntent.getBroadcast(context,id,
        Intent(context,PrayerAlarmReceiver::class.java).setAction("com.example.PRAYER").putExtra("name",name).putExtra("exact",exact), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    @Synchronized fun clear(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = PrayerPreferences.prefs(context)
        val old = JSONArray(prefs.getString("alarms", "[]"))
        for(i in 0 until old.length()) manager.cancel(pending(context,1000+i))
        prefs.edit().putString("alarms","[]").apply()
    }
    @Synchronized fun replace(context: Context, alarms: JSONArray) {
        clear(context)
        PrayerPreferences.prefs(context).edit().putString("alarms",alarms.toString()).apply()
        restore(context)
    }
    @SuppressLint("ScheduleExactAlarm")
    @Synchronized fun restore(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val settings = context.getSharedPreferences("settings_prefs",Context.MODE_PRIVATE)
        val alarms = JSONArray(PrayerPreferences.prefs(context).getString("alarms", "[]"))
        for(i in 0 until alarms.length()) {
            val alarm=alarms.getJSONObject(i); val name=alarm.getString("name"); val at=alarm.getLong("time")
            manager.cancel(pending(context,1000+i))
            if(at<=System.currentTimeMillis() || !settings.getBoolean("${name.lowercase(Locale.ROOT)}_enabled",true)) continue
            val exact=Build.VERSION.SDK_INT<31 || manager.canScheduleExactAlarms()
            try {
                if(exact) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending(context,1000+i,name,true))
                else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending(context,1000+i,name,false))
            } catch (_: SecurityException) { manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending(context,1000+i,name,false)) }
        }
    }
    fun notification(context: Context, name: String, playback: Boolean): Notification {
        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel=if(playback) "azan_playback" else "prayer_reminder"
        if(Build.VERSION.SDK_INT>=26) {
            val c=NotificationChannel(channel,if(playback) "Azan playback" else "Prayer reminders",NotificationManager.IMPORTANCE_DEFAULT)
            if(playback) c.setSound(null,null)
            manager.createNotificationChannel(c)
        }
        val content=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop=PendingIntent.getService(context,0,Intent(context,AzanPlaybackService::class.java).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder=NotificationCompat.Builder(context,channel).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("$name • نماز کا وقت")
            .setContentText(if(playback) "Azan playing — tap Stop to silence" else "It is time for prayer.").setContentIntent(content).setAutoCancel(!playback).setOngoing(playback)
        if(playback) builder.addAction(0,"Stop Azan",stop)
        return builder.build()
    }
}
class PrayerAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        if(intent.action!="com.example.PRAYER") {
            PrayerAlerts.restore(context)
            PrayerPreferences.startWorker(context)
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<PrayerRefreshWorker>().build())
            return
        }
        val name=intent.getStringExtra("name") ?: return
        val prefs=context.getSharedPreferences("settings_prefs",Context.MODE_PRIVATE)
        if(!prefs.getBoolean("${name.lowercase(Locale.ROOT)}_enabled",true)) return
        val sound=prefs.getString("azan_sound","Classic")
        // Only exact, user-requested alarms qualify for background playback. Inexact alarms notify only.
        if(sound!="Default" && intent.getBooleanExtra("exact",false)) {
            try { ContextCompat.startForegroundService(context,Intent(context,AzanPlaybackService::class.java).putExtra("name",name)); return }
            catch (_: RuntimeException) { /* Android may deny playback; retain the reminder. */ }
        }
        try { (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(name.hashCode(),PrayerAlerts.notification(context,name,false)) }
        catch (_: SecurityException) { }
    }
}
class AzanPlaybackService: Service() {
    private var player: MediaPlayer?=null
    private lateinit var audio: AudioManager
    private var focus: AudioFocusRequest?=null
    private val focusListener=AudioManager.OnAudioFocusChangeListener { change -> if(change<0) stopSelf() }
    override fun onBind(intent: Intent?): IBinder?=null
    override fun onCreate() { super.onCreate(); audio=getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    override fun onStartCommand(intent: Intent?,flags: Int,startId: Int): Int {
        if(intent?.action=="STOP") { stopSelf(); return START_NOT_STICKY }
        val name=intent?.getStringExtra("name") ?: "Azan preview"
        startForeground(777,PrayerAlerts.notification(this,name,true))
        val attrs=AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        val result=if(Build.VERSION.SDK_INT>=26) {
            focus=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs).setOnAudioFocusChangeListener(focusListener).build()
            audio.requestAudioFocus(focus!!)
        } else { @Suppress("DEPRECATION") audio.requestAudioFocus(focusListener,AudioManager.STREAM_ALARM,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) }
        if(result!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { stopSelf(); return START_NOT_STICKY }
        player?.release()
        val prefs=getSharedPreferences("settings_prefs",Context.MODE_PRIVATE)
        val custom=prefs.getString("custom_azan_uri",null)
        val uri=if(prefs.getString("azan_sound","Classic")=="Custom" && custom!=null) Uri.parse(custom)
            else Uri.parse("android.resource://$packageName/${R.raw.azan_classic}")
        try {
            player=MediaPlayer().apply {
                setAudioAttributes(attrs); setWakeMode(this@AzanPlaybackService,PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(this@AzanPlaybackService,uri)
                setOnPreparedListener { it.start() }; setOnCompletionListener { stopSelf() }
                setOnErrorListener { _,_,_ -> stopSelf(); true }; prepareAsync()
            }
        } catch (_: Exception) { stopSelf() }
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        player?.release(); player=null
        if(Build.VERSION.SDK_INT>=26) focus?.let { audio.abandonAudioFocusRequest(it) }
        else { @Suppress("DEPRECATION") audio.abandonAudioFocus(focusListener) }
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy()
    }
}
