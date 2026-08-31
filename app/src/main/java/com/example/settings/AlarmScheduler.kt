package com.example.settings

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.remote.Timings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarmsForTimings(context: Context, timings: Timings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Since Android 12 (API 31), apps need SCHEDULE_EXACT_ALARM permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmScheduler", "Cannot schedule exact alarms. Permission denied.")
            return
        }

        schedulePrayerAlarm(context, alarmManager, "Fajr", timings.Fajr)
        schedulePrayerAlarm(context, alarmManager, "Dhuhr", timings.Dhuhr)
        schedulePrayerAlarm(context, alarmManager, "Asr", timings.Asr)
        schedulePrayerAlarm(context, alarmManager, "Maghrib", timings.Maghrib)
        schedulePrayerAlarm(context, alarmManager, "Isha", timings.Isha)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun schedulePrayerAlarm(context: Context, alarmManager: AlarmManager, prayerName: String, timeString: String) {
        try {
            // timeString format from API is usually "HH:mm"
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = format.parse(timeString.take(5)) ?: return
            
            val calendar = Calendar.getInstance()
            val now = Calendar.getInstance()
            
            val timeCalendar = Calendar.getInstance().apply { time = date }
            
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            
            // If the time has already passed today, don't schedule it for today (could schedule for tomorrow)
            if (calendar.before(now)) {
                return
            }

            val intent = Intent(context, AzanBroadcastReceiver::class.java).apply {
                putExtra("PRAYER_NAME", prayerName)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayerName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel any existing alarm for this prayer
            alarmManager.cancel(pendingIntent)

            // Schedule new alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Scheduled $prayerName alarm at ${calendar.time}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
