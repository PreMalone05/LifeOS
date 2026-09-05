package com.example.notification

import android.content.Context
import android.util.Log
import com.example.data.LifeDao
import com.example.data.LifeDatabase
import com.example.data.PredictiveNotificationHistoryEntity
import com.example.data.PredictiveRecommendation
import com.example.data.RecommendationType
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.util.Calendar

object PredictiveNotificationManager {
    private const val TAG = "PredictiveNotifManager"

    // Cooldown duration: 3 hours
    const val COOLDOWN_MILLIS = 3 * 60 * 60 * 1000L

    // Deduplication duration: 12 hours
    const val DEDUPLICATION_MILLIS = 12 * 60 * 60 * 1000L

    // Maximum proactive notifications allowed globally per calendar day
    const val MAX_DAILY_PROACTIVE_NOTIFICATIONS = 4

    // Prune cutoff: 14 days
    const val PRUNE_CUTOFF_MILLIS = 14 * 24 * 60 * 60 * 1000L

    // Synchronized lock for process-level thread safety during check & dispatch
    private val dispatchLock = Any()

    fun computeDeduplicationHash(recommendation: PredictiveRecommendation): String {
        val raw = "${recommendation.type.name}_${recommendation.title.trim()}_${recommendation.suggestedAction.trim()}"
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            raw.hashCode().toString()
        }
    }

    fun shouldSendNotification(
        context: Context,
        recommendation: PredictiveRecommendation,
        notificationsEnabled: Boolean = true,
        quietHoursEnabled: Boolean = true,
        startQuietHour: Int = 22,
        endQuietHour: Int = 7,
        daoOverride: LifeDao? = null,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean = synchronized(dispatchLock) {
        if (!notificationsEnabled) return false
        if (!SmartNotificationManager.hasPermission(context)) return false

        // Check quiet hours
        if (quietHoursEnabled) {
            val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val inQuietHours = if (startQuietHour > endQuietHour) {
                currentHour >= startQuietHour || currentHour < endQuietHour
            } else {
                currentHour in startQuietHour until endQuietHour
            }
            if (inQuietHours && recommendation.priority != "CRITICAL") {
                Log.d(TAG, "Notification skipped due to quiet hours: ${recommendation.title}")
                return false
            }
        }

        val dao = daoOverride ?: try {
            LifeDatabase.getDatabase(context).lifeDao()
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing database for notification cooldown check", e)
            return false
        }

        val hash = computeDeduplicationHash(recommendation)

        return runBlocking {
            try {
                // 1. Enforce global daily quota (maximum 4 notifications per calendar day)
                val cal = Calendar.getInstance().apply {
                    timeInMillis = currentTimeMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDayMillis = cal.timeInMillis
                val dispatchedToday = dao.getDispatchedCountSince(startOfDayMillis)
                if (dispatchedToday >= MAX_DAILY_PROACTIVE_NOTIFICATIONS) {
                    Log.d(TAG, "Notification skipped due to daily quota: $dispatchedToday >= $MAX_DAILY_PROACTIVE_NOTIFICATIONS")
                    return@runBlocking false
                }

                // 2. Check deduplication hash (within 12h)
                val lastHashTime = dao.getLastDispatchedTimestampForHash(hash) ?: 0L
                if (currentTimeMillis - lastHashTime < DEDUPLICATION_MILLIS) {
                    Log.d(TAG, "Notification skipped due to persisted deduplication: $hash")
                    return@runBlocking false
                }

                // 3. Check Cooldown by Type (within 3h)
                val lastTypeTime = dao.getLastDispatchedTimestampForType(recommendation.type.name) ?: 0L
                if (currentTimeMillis - lastTypeTime < COOLDOWN_MILLIS) {
                    Log.d(TAG, "Notification skipped due to persisted type cooldown: ${recommendation.type.name}")
                    return@runBlocking false
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Database query failed during notification check", e)
                false
            }
        }
    }

    fun sendPredictiveAlert(
        context: Context,
        recommendation: PredictiveRecommendation,
        notificationsEnabled: Boolean = true,
        quietHoursEnabled: Boolean = true,
        startQuietHour: Int = 22,
        endQuietHour: Int = 7,
        daoOverride: LifeDao? = null,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean = synchronized(dispatchLock) {
        if (!shouldSendNotification(
                context = context,
                recommendation = recommendation,
                notificationsEnabled = notificationsEnabled,
                quietHoursEnabled = quietHoursEnabled,
                startQuietHour = startQuietHour,
                endQuietHour = endQuietHour,
                daoOverride = daoOverride,
                currentTimeMillis = currentTimeMillis
            )
        ) {
            return false
        }

        val title = when (recommendation.type) {
            RecommendationType.CAPACITY_WARNING -> "⚠️ ${recommendation.title}"
            RecommendationType.DEADLINE_WARNING -> "⏰ ${recommendation.title}"
            RecommendationType.FOCUS_WINDOW -> "🎯 ${recommendation.title}"
            RecommendationType.HABIT_RISK -> "🌱 ${recommendation.title}"
            RecommendationType.PLAN_DIVERGENCE -> "⚡ ${recommendation.title}"
            else -> "💡 ${recommendation.title}"
        }

        val message = "${recommendation.explanation} ${recommendation.suggestedAction}"
        SmartNotificationManager.sendNotification(context, title, message)

        val hash = computeDeduplicationHash(recommendation)
        val dao = daoOverride ?: try {
            LifeDatabase.getDatabase(context).lifeDao()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get DAO to persist notification dispatch", e)
            null
        }

        if (dao != null) {
            runBlocking {
                try {
                    dao.insertNotificationHistory(
                        PredictiveNotificationHistoryEntity(
                            recommendationType = recommendation.type.name,
                            deduplicationHash = hash,
                            dispatchedTimestamp = currentTimeMillis
                        )
                    )
                    // Prune history older than 14 days
                    dao.pruneOldNotificationHistory(currentTimeMillis - PRUNE_CUTOFF_MILLIS)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist notification dispatch history", e)
                }
            }
        }

        return true
    }

    fun clearHistory(context: Context, daoOverride: LifeDao? = null) = synchronized(dispatchLock) {
        val dao = daoOverride ?: try {
            LifeDatabase.getDatabase(context).lifeDao()
        } catch (e: Exception) {
            null
        }

        if (dao != null) {
            runBlocking {
                try {
                    dao.clearNotificationHistory()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to clear notification history", e)
                }
            }
        }
    }
}
