package com.ozcomingfroo.mybudget.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
) {
    fun schedule(preferences: AppPreferences) {
        if (preferences.dailyReminderEnabled) {
            schedule(preferences.dailyReminderHour, preferences.dailyReminderMinute)
        } else {
            cancel()
        }
    }

    fun schedule(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(clock, hour, minute),
            reminderPendingIntent(),
        )
    }

    fun cancel() {
        context.getSystemService(AlarmManager::class.java).cancel(reminderPendingIntent())
    }

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_SHOW_DAILY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            ReminderRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_SHOW_DAILY_REMINDER =
            "com.ozcomingfroo.mybudget.action.SHOW_DAILY_REMINDER"

        private const val ReminderRequestCode = 3000

        internal fun nextTriggerMillis(clock: Clock, hour: Int, minute: Int): Long {
            val zone = clock.zone.takeUnless { it == ZoneId.of("Z") } ?: ZoneId.systemDefault()
            val now = LocalDateTime.now(clock.withZone(zone))
            val reminderTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            var next = now.toLocalDate().atTime(reminderTime)
            if (!next.isAfter(now)) {
                next = next.plusDays(1)
            }
            return next.atZone(zone).toInstant().toEpochMilli()
        }
    }
}
