package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoisePlayer
import com.example.audio.NoiseType
import com.example.auth.GoogleAuthHelper
import com.example.data.LifeDatabase
import com.example.data.TaskEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LifeOS", appName)
  }

  @Test
  fun `google auth helper handles unconfigured environment gracefully`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val isConfigured = GoogleAuthHelper.isConfigured(context)
    assertFalse(isConfigured)

    val result = GoogleAuthHelper.signInWithGoogle(context)
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception?.message?.contains("Google Sign-In is not configured") == true)
  }

  @Test
  fun `noise player initializes with proper defaults`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val player = NoisePlayer(context)
    assertEquals(NoiseType.OFF, player.getCurrentType())
    assertEquals(0.5f, player.getVolume(), 0.01f)
    assertFalse(player.isPlaying())
    
    player.setVolume(0.8f)
    assertEquals(0.8f, player.getVolume(), 0.01f)
    player.stop()
  }

  @Test
  fun `database schema v7 can insert and read entities in memory`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, LifeDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.lifeDao()
    val testProfile = UserProfileEntity(
      id = 1,
      name = "Test User",
      coachPersonality = "The Stoic Mentor",
      currentVibe = "Focused"
    )
    dao.insertUserProfile(testProfile)

    val loadedProfile = dao.getUserProfile().first()
    assertNotNull(loadedProfile)
    assertEquals("Test User", loadedProfile?.name)

    val testTask = TaskEntity(
      title = "P1 Architecture Verification",
      category = "WORK",
      timeSlot = "10:00 - 11:00 AM",
      description = "Verify database integrity",
      date = "2026-08-25",
      location = "Headquarters"
    )
    val taskId = dao.insertTask(testTask)
    assertTrue(taskId > 0)

    val tasks = dao.getAllTasks().first()
    assertEquals(1, tasks.size)
    assertEquals("Headquarters", tasks[0].location)

    db.close()
  }

  @Test
  fun `fresh database starts completely clean without dummy items`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, LifeDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.lifeDao()
    val tasks = dao.getAllTasks().first()
    val goals = dao.getAllGoals().first()
    val habits = dao.getAllHabits().first()
    val profile = dao.getUserProfile().first()

    assertTrue(tasks.isEmpty())
    assertTrue(goals.isEmpty())
    assertTrue(habits.isEmpty())
    assertEquals(null, profile)

    db.close()
  }

  @Test
  fun `un-onboarded profile defaults are preserved and habits start at 0 streak`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, LifeDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.lifeDao()
    val profile = UserProfileEntity(
      id = 1,
      userId = "local_user",
      name = "",
      isOnboarded = false
    )
    dao.insertUserProfile(profile)

    val loadedProfile = dao.getUserProfile().first()
    assertNotNull(loadedProfile)
    assertFalse(loadedProfile!!.isOnboarded)
    assertEquals("", loadedProfile.name)

    val habit = com.example.data.HabitEntity(
      name = "Morning Hydration",
      currentValue = 0f,
      targetValue = 2.5f,
      unit = "L",
      iconName = "water_drop"
    )
    dao.insertHabit(habit)

    val loadedHabits = dao.getAllHabits().first()
    assertEquals(1, loadedHabits.size)
    assertEquals(0, loadedHabits[0].streak)

    db.close()
  }

  @Test
  fun `database schema v9 can persist behavioral events and performance records`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, LifeDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.lifeDao()

    val event = com.example.data.BehavioralEventEntity(
      eventType = "TASK_COMPLETED",
      category = "WORK",
      priority = "CRITICAL",
      timeOfDayHour = 10,
      dayOfWeek = 1,
      metadataJson = """{"taskId": 101}""",
      date = "2026-08-27"
    )
    val eventId = dao.insertBehavioralEvent(event)
    assertTrue(eventId > 0)

    val performance = com.example.data.TaskPerformanceRecordEntity(
      taskId = 101,
      category = "WORK",
      estimatedMinutes = 45,
      actualMinutes = 50,
      estimationErrorMinutes = 5,
      priority = "CRITICAL",
      energyLevel = "HIGH",
      timeSlotHour = 10,
      dayOfWeek = 1,
      date = "2026-08-27"
    )
    val perfId = dao.insertTaskPerformanceRecord(performance)
    assertTrue(perfId > 0)

    val feedback = com.example.data.RecommendationFeedbackEntity(
      recommendationType = "CAPACITY_WARNING",
      recommendationText = "Plan max 5 hours",
      feedback = "HELPFUL"
    )
    val feedbackId = dao.insertRecommendationFeedback(feedback)
    assertTrue(feedbackId > 0)

    val events = dao.getAllBehavioralEvents().first()
    assertEquals(1, events.size)
    assertEquals("TASK_COMPLETED", events[0].eventType)

    val records = dao.getAllTaskPerformanceRecords().first()
    assertEquals(1, records.size)
    assertEquals(50, records[0].actualMinutes)

    val feedbacks = dao.getAllRecommendationFeedback().first()
    assertEquals(1, feedbacks.size)
    assertEquals("HELPFUL", feedbacks[0].feedback)

    db.close()
  }
}

