package com.ozcomingfroo.mybudget.reminders

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyReminderSchedulerTest {
    private val zone = ZoneId.of("Asia/Jerusalem")

    @Test
    fun nextTriggerMillis_beforeReminderTime_returnsTodayAtReminderTime() {
        val clock = fixedClock("2026-06-29T16:00:00Z")

        val trigger = DailyReminderScheduler.nextTriggerMillis(clock, 20, 0)

        assertEquals(
            ZonedDateTime.of(2026, 6, 29, 20, 0, 0, 0, zone).toInstant().toEpochMilli(),
            trigger,
        )
    }

    @Test
    fun nextTriggerMillis_afterReminderTime_returnsTomorrowAtReminderTime() {
        val clock = fixedClock("2026-06-29T19:00:00Z")

        val trigger = DailyReminderScheduler.nextTriggerMillis(clock, 20, 0)

        assertEquals(
            ZonedDateTime.of(2026, 6, 30, 20, 0, 0, 0, zone).toInstant().toEpochMilli(),
            trigger,
        )
    }

    @Test
    fun nextTriggerMillis_respectsCustomReminderTime() {
        val clock = fixedClock("2026-06-29T04:00:00Z")

        val trigger = DailyReminderScheduler.nextTriggerMillis(clock, 8, 30)

        assertEquals(
            ZonedDateTime.of(2026, 6, 29, 8, 30, 0, 0, zone).toInstant().toEpochMilli(),
            trigger,
        )
    }

    private fun fixedClock(instant: String): Clock =
        Clock.fixed(Instant.parse(instant), zone)
}
