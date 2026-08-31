package com.example.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class AzanBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Re-schedule all alarms here if we had persistence logic for times
            return
        }

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("${prayerName.lowercase()}_enabled", true)
        
        if (!isEnabled) return
        
        val azanSoundName = prefs.getString("azan_sound", "Default") ?: "Default"
        
        val soundUri: Uri = if (azanSoundName != "Default") {
            // In a real app, you would place raw files in res/raw (e.g., azan_makkah.mp3)
            // For now, we attempt to resolve a raw resource or fallback to system default
            val resId = context.resources.getIdentifier(
                "azan_${azanSoundName.lowercase().replace(" ", "_")}",
                "raw",
                context.packageName
            )
            if (resId != 0) {
                Uri.parse("android.resource://${context.packageName}/$resId")
            } else {
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            }
        } else {
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        }

        // Channel ID must be unique per sound because you can't change sound on an existing channel
        val channelId = "azan_channel_${azanSoundName.replace(" ", "_").lowercase()}"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = notificationManager.getNotificationChannel(channelId)
            if (channel == null) {
                channel = NotificationChannel(
                    channelId,
                    "Azan Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Prayer time notifications"
                    val audioAttr = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .build()
                    setSound(soundUri, audioAttr)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName Prayer Time")
            .setContentText("It is time for $prayerName prayer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }
}
