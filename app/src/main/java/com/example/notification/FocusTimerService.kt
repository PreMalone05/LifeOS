package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "focus_timer_fg_channel"
        const val NOTIFICATION_ID = 5001

        const val ACTION_START = "ACTION_START_FOCUS_TIMER"
        const val ACTION_PAUSE = "ACTION_PAUSE_FOCUS_TIMER"
        const val ACTION_STOP = "ACTION_STOP_FOCUS_TIMER"
        const val EXTRA_DURATION_SECONDS = "EXTRA_DURATION_SECONDS"
        const val EXTRA_FOCUS_TARGET = "EXTRA_FOCUS_TARGET"

        private val _isTimerRunning = MutableStateFlow(false)
        val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

        private val _timeLeftSeconds = MutableStateFlow(1500) // Default 25 min
        val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

        private val _targetEndTimeMillis = MutableStateFlow(0L)
        val targetEndTimeMillis: StateFlow<Long> = _targetEndTimeMillis.asStateFlow()

        private val _focusTarget = MutableStateFlow("Deep Focus")
        val focusTarget: StateFlow<String> = _focusTarget.asStateFlow()

        fun startTimerService(context: Context, durationSeconds: Int, targetText: String = "Deep Focus") {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                putExtra(EXTRA_FOCUS_TARGET, targetText)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimerService(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun stopTimerService(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateTimeLeftManual(seconds: Int) {
            _timeLeftSeconds.value = seconds
        }

        fun updateFocusTargetManual(target: String) {
            _focusTarget.value = target
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, _timeLeftSeconds.value)
                val targetText = intent.getStringExtra(EXTRA_FOCUS_TARGET) ?: _focusTarget.value
                _focusTarget.value = targetText
                startTimer(duration)
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Deep Focus Timer"
            val desc = "Live counting notification for deep focus sessions"
            val importance = NotificationManager.IMPORTANCE_LOW // Low so it doesn't chime on every second update
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTimer(durationSeconds: Int) {
        val now = System.currentTimeMillis()
        val targetEnd = now + (durationSeconds * 1000L)
        _targetEndTimeMillis.value = targetEnd
        _timeLeftSeconds.value = durationSeconds
        _isTimerRunning.value = true

        // Start Foreground Notification
        startForeground(NOTIFICATION_ID, buildNotification(durationSeconds))

        // Launch accuracy ticker loop
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (_isTimerRunning.value) {
                val currentNow = System.currentTimeMillis()
                val remaining = maxOf(0L, (_targetEndTimeMillis.value - currentNow) / 1000L).toInt()
                _timeLeftSeconds.value = remaining

                updateNotification(remaining)

                if (remaining <= 0) {
                    _isTimerRunning.value = false
                    onTimerFinished()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        tickerJob?.cancel()
        tickerJob = null

        // Update notification to show paused state
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(_timeLeftSeconds.value, isPaused = true))
    }

    private fun stopTimer() {
        _isTimerRunning.value = false
        tickerJob?.cancel()
        tickerJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        // Send Completion High Priority Notification
        SmartNotificationManager.sendNotification(
            this,
            "🎉 Focus Session Completed!",
            "Great work on '${_focusTarget.value}'! +100 XP awarded to your LifeOS profile."
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(secondsLeft: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(secondsLeft))
    }

    private fun buildNotification(secondsLeft: Int, isPaused: Boolean = false): android.app.Notification {
        val minutes = secondsLeft / 60
        val secs = secondsLeft % 60
        val timeFormatted = String.format("%02d:%02d", minutes, secs)

        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingAppIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause Action Intent
        val pauseIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_PAUSE }
        val pendingPauseIntent = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Resume Action Intent
        val resumeIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_DURATION_SECONDS, secondsLeft)
        }
        val pendingResumeIntent = PendingIntent.getService(
            this,
            2,
            resumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action Intent
        val stopIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_STOP }
        val pendingStopIntent = PendingIntent.getService(
            this,
            3,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPaused) "⏸ Focus Timer Paused ($timeFormatted)" else "🔥 Focus Timer Running ($timeFormatted)"
        val contentText = "Target: ${_focusTarget.value}"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(!isPaused)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingAppIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isPaused) {
            builder.addAction(android.R.drawable.ic_media_play, "Resume", pendingResumeIntent)
        } else {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pendingPauseIntent)
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingStopIntent)

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
