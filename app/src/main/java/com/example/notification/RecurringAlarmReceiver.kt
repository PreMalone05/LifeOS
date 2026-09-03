package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import com.example.data.LifeDatabase
import com.example.data.RecurringAlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecurringAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("RecurringAlarmReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = LifeDatabase.getDatabase(context)
                    val enabledAlarms = db.lifeDao().getEnabledRecurringAlarms()
                    for (alarm in enabledAlarms) {
                        AlarmSchedulerManager.scheduleAlarm(context, alarm)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == AlarmSchedulerManager.ACTION_SNOOZE) {
            val alarmId = intent.getIntExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, -1)
            val title = intent.getStringExtra(AlarmSchedulerManager.EXTRA_ALARM_TITLE) ?: "Alarm"
            val message = intent.getStringExtra(AlarmSchedulerManager.EXTRA_ALARM_MESSAGE) ?: "Reminder"
            AlarmSchedulerManager.snoozeAlarm(context, alarmId, title, message, 5)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(alarmId.takeIf { it > 0 } ?: 4001)
            android.widget.Toast.makeText(context, "⏰ Snoozed for 5 minutes", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (action == AlarmSchedulerManager.ACTION_DISMISS) {
            val alarmId = intent.getIntExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, -1)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(alarmId.takeIf { it > 0 } ?: 4001)
            android.widget.Toast.makeText(context, "⏰ Alarm Dismissed", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (action == AlarmSchedulerManager.ACTION_ALARM) {
            val alarmId = intent.getIntExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, -1)
            val title = intent.getStringExtra(AlarmSchedulerManager.EXTRA_ALARM_TITLE) ?: "Scheduled Alarm"
            val message = intent.getStringExtra(AlarmSchedulerManager.EXTRA_ALARM_MESSAGE) ?: "Time for your scheduled focus and goal session!"
            val hour = intent.getIntExtra(AlarmSchedulerManager.EXTRA_ALARM_HOUR, 8)
            val minute = intent.getIntExtra(AlarmSchedulerManager.EXTRA_ALARM_MINUTE, 0)
            val repeatType = intent.getStringExtra(AlarmSchedulerManager.EXTRA_ALARM_REPEAT_TYPE) ?: "DAILY"
            val soundEnabled = intent.getBooleanExtra(AlarmSchedulerManager.EXTRA_ALARM_SOUND, true)

            // 1. Send High Priority Notification with Snooze and Dismiss actions
            SmartNotificationManager.sendAlarmNotificationWithActions(
                context = context,
                alarmId = alarmId,
                title = title,
                message = message
            )

            // 2. Launch Fullscreen Alarm Ringer Activity
            try {
                val ringerIntent = Intent(context, AlarmRingerActivity::class.java).apply {
                    putExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmSchedulerManager.EXTRA_ALARM_TITLE, title)
                    putExtra(AlarmSchedulerManager.EXTRA_ALARM_MESSAGE, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(ringerIntent)
            } catch (e: Exception) {
                Log.e("RecurringAlarmReceiver", "Could not start AlarmRingerActivity directly", e)
            }

            // 3. Reschedule next trigger if recurring
            if (repeatType != "TEST" && repeatType != "SNOOZE" && alarmId > 0) {
                val alarmToReschedule = RecurringAlarmEntity(
                    id = alarmId,
                    title = title,
                    message = message,
                    hour = hour,
                    minute = minute,
                    repeatType = repeatType,
                    isEnabled = true,
                    soundEnabled = soundEnabled
                )
                AlarmSchedulerManager.scheduleAlarm(context, alarmToReschedule)
            }
        }
    }
}
