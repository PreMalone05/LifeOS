package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.RecurringAlarmEntity
import java.util.Calendar

object AlarmSchedulerManager {
    const val ACTION_ALARM = "com.example.ACTION_RECURRING_ALARM_TRIGGER"
    const val ACTION_SNOOZE = "com.example.ACTION_RECURRING_ALARM_SNOOZE"
    const val ACTION_DISMISS = "com.example.ACTION_RECURRING_ALARM_DISMISS"

    const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
    const val EXTRA_ALARM_TITLE = "EXTRA_ALARM_TITLE"
    const val EXTRA_ALARM_MESSAGE = "EXTRA_ALARM_MESSAGE"
    const val EXTRA_ALARM_HOUR = "EXTRA_ALARM_HOUR"
    const val EXTRA_ALARM_MINUTE = "EXTRA_ALARM_MINUTE"
    const val EXTRA_ALARM_REPEAT_TYPE = "EXTRA_ALARM_REPEAT_TYPE"
    const val EXTRA_ALARM_SOUND = "EXTRA_ALARM_SOUND"

    fun scheduleAlarm(context: Context, alarm: RecurringAlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_TITLE, alarm.title)
            putExtra(EXTRA_ALARM_MESSAGE, alarm.message)
            putExtra(EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(EXTRA_ALARM_REPEAT_TYPE, alarm.repeatType)
            putExtra(EXTRA_ALARM_SOUND, alarm.soundEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val targetTime = calculateNextTriggerMillis(alarm.hour, alarm.minute, alarm.repeatType)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            }
            Log.d("AlarmSchedulerManager", "Scheduled alarm ${alarm.id} for $targetTime")
        } catch (e: Exception) {
            e.printStackTrace()
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetTime,
                pendingIntent
            )
        }
    }

    fun snoozeAlarm(context: Context, alarmId: Int, title: String, message: String, snoozeMinutes: Int = 5) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_TITLE, title)
            putExtra(EXTRA_ALARM_MESSAGE, "$message (Snoozed)")
            putExtra(EXTRA_ALARM_HOUR, 0)
            putExtra(EXTRA_ALARM_MINUTE, 0)
            putExtra(EXTRA_ALARM_REPEAT_TYPE, "SNOOZE")
            putExtra(EXTRA_ALARM_SOUND, true)
        }

        val requestCode = if (alarmId > 0) alarmId + 88888 else 99998
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d("AlarmSchedulerManager", "Snoozed alarm $alarmId for $snoozeMinutes minutes")
        } catch (e: Exception) {
            e.printStackTrace()
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun scheduleTestAlarm(context: Context, title: String, message: String, delaySeconds: Int = 5) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_ALARM_ID, 9999)
            putExtra(EXTRA_ALARM_TITLE, title)
            putExtra(EXTRA_ALARM_MESSAGE, message)
            putExtra(EXTRA_ALARM_HOUR, 0)
            putExtra(EXTRA_ALARM_MINUTE, 0)
            putExtra(EXTRA_ALARM_REPEAT_TYPE, "TEST")
            putExtra(EXTRA_ALARM_SOUND, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun calculateNextTriggerMillis(hour: Int, minute: Int, repeatType: String): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        when (repeatType) {
            "WEEKDAYS" -> {
                while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            "WEEKENDS" -> {
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return calendar.timeInMillis
    }
}
