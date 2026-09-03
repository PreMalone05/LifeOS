package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object SmartNotificationManager {
    const val CHANNEL_ID = "smart_lifeos_notifications"
    private const val CHANNEL_NAME = "Smart LifeOS Notifications"
    private const val CHANNEL_DESC = "Smart reminders, habit check-ins, and AI coach nudges."
    private const val NOTIFICATION_ID = 4001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        // Only send if permission is granted (or if below Android 13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use Android system default resources for the icon, or the app's default launcher icon
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System standard fallback icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle edge case where permission check bypassed but threw
            e.printStackTrace()
        }
    }

    fun sendAlarmNotificationWithActions(
        context: Context,
        alarmId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission(context)) {
            return
        }

        // Fullscreen Intent to launch AlarmRingerActivity
        val fullScreenIntent = Intent(context, AlarmRingerActivity::class.java).apply {
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_MESSAGE, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Pending Intent
        val snoozeIntent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = AlarmSchedulerManager.ACTION_SNOOZE
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_MESSAGE, message)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 1001,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Pending Intent
        val dismissIntent = Intent(context, RecurringAlarmReceiver::class.java).apply {
            action = AlarmSchedulerManager.ACTION_DISMISS
            putExtra(AlarmSchedulerManager.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 1002,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "SNOOZE 5M", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_delete, "DISMISS", dismissPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(alarmId.takeIf { it > 0 } ?: NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
