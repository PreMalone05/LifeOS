package com.example.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class CalendarEvent(
    val id: Long,
    val eventId: Long,
    val title: String,
    val description: String?,
    val location: String?,
    val startMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
    val calendarName: String?,
    val formattedTime: String,
    val durationMinutes: Int = if (endMillis > startMillis) ((endMillis - startMillis) / 60000L).toInt() else 60,
    val startHour: Int = {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startMillis
        cal.get(Calendar.HOUR_OF_DAY)
    }()
)

object CalendarManager {
    private const val TAG = "CalendarManager"

    private val INSTANCE_PROJECTION = arrayOf(
        CalendarContract.Instances.EVENT_ID,          // 0
        CalendarContract.Instances.TITLE,             // 1
        CalendarContract.Instances.DESCRIPTION,       // 2
        CalendarContract.Instances.EVENT_LOCATION,     // 3
        CalendarContract.Instances.BEGIN,              // 4
        CalendarContract.Instances.END,                // 5
        CalendarContract.Instances.ALL_DAY,            // 6
        CalendarContract.Instances.CALENDAR_DISPLAY_NAME // 7
    )

    private const val PROJECTION_EVENT_ID_INDEX = 0
    private const val PROJECTION_TITLE_INDEX = 1
    private const val PROJECTION_DESCRIPTION_INDEX = 2
    private const val PROJECTION_LOCATION_INDEX = 3
    private const val PROJECTION_BEGIN_INDEX = 4
    private const val PROJECTION_END_INDEX = 5
    private const val PROJECTION_ALL_DAY_INDEX = 6
    private const val PROJECTION_CALENDAR_NAME_INDEX = 7

    fun fetchEventsForDate(context: Context, dateString: String): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        
        // Parse date string (YYYY-MM-DD)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            sdf.parse(dateString)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date string: $dateString", e)
            null
        } ?: return emptyList()

        // Calculate start and end millis of that day
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis

        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMillis = calendar.timeInMillis

        val contentResolver = context.contentResolver
        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            if (cursor != null) {
                var uniqueId = 0L
                while (cursor.moveToNext()) {
                    val eventId = cursor.getLong(PROJECTION_EVENT_ID_INDEX)
                    val title = cursor.getString(PROJECTION_TITLE_INDEX) ?: "Untitled Event"
                    val description = cursor.getString(PROJECTION_DESCRIPTION_INDEX)
                    val location = cursor.getString(PROJECTION_LOCATION_INDEX)
                    val begin = cursor.getLong(PROJECTION_BEGIN_INDEX)
                    val end = cursor.getLong(PROJECTION_END_INDEX)
                    val allDay = cursor.getInt(PROJECTION_ALL_DAY_INDEX) == 1
                    val calendarName = cursor.getString(PROJECTION_CALENDAR_NAME_INDEX) ?: "Primary"

                    val formattedTime = if (allDay) {
                        "All Day"
                    } else {
                        val startTimeStr = timeFormat.format(Date(begin))
                        val endTimeStr = timeFormat.format(Date(end))
                        "$startTimeStr - $endTimeStr"
                    }

                    events.add(
                        CalendarEvent(
                            id = uniqueId++,
                            eventId = eventId,
                            title = title,
                            description = description,
                            location = location,
                            startMillis = begin,
                            endMillis = end,
                            allDay = allDay,
                            calendarName = calendarName,
                            formattedTime = formattedTime
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to read calendar provider", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying calendar provider", e)
        } finally {
            cursor?.close()
        }

        return events
    }

    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String,
        startMillis: Long,
        endMillis: Long
    ): Uri? {
        val contentResolver = context.contentResolver
        val values = android.content.ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.CALENDAR_ID, 1) // default primary calendar ID
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        return try {
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to write calendar", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting event to calendar", e)
            null
        }
    }
}
