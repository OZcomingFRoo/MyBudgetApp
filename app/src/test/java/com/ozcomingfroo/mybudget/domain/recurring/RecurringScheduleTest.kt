package com.ozcomingfroo.mybudget.domain.recurring

import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringScheduleTest {
    @Test
    fun nextStartDate_weeklyTodayRunsToday() {
        val today = LocalDate.of(2026, 6, 15)

        val next = RecurringSchedule.nextStartDate(
            frequency = RecurringFrequency.WEEKLY,
            today = today,
            scheduleWeekday = DayOfWeek.MONDAY.value,
            scheduleMonthDay = null,
        )

        assertEquals(today, next)
    }

    @Test
    fun nextStartDate_weeklyPastDayRunsNextWeek() {
        val next = RecurringSchedule.nextStartDate(
            frequency = RecurringFrequency.WEEKLY,
            today = LocalDate.of(2026, 6, 17),
            scheduleWeekday = DayOfWeek.MONDAY.value,
            scheduleMonthDay = null,
        )

        assertEquals(LocalDate.of(2026, 6, 22), next)
    }

    @Test
    fun nextStartDate_monthlyTodayRunsToday() {
        val today = LocalDate.of(2026, 6, 16)

        val next = RecurringSchedule.nextStartDate(
            frequency = RecurringFrequency.MONTHLY,
            today = today,
            scheduleWeekday = null,
            scheduleMonthDay = 16,
        )

        assertEquals(today, next)
    }

    @Test
    fun nextStartDate_monthlyPastDayRunsNextMonth() {
        val next = RecurringSchedule.nextStartDate(
            frequency = RecurringFrequency.MONTHLY,
            today = LocalDate.of(2026, 6, 16),
            scheduleWeekday = null,
            scheduleMonthDay = 15,
        )

        assertEquals(LocalDate.of(2026, 7, 15), next)
    }

    @Test
    fun nextStartDate_monthlyDay30ClampsInFebruary() {
        val next = RecurringSchedule.nextStartDate(
            frequency = RecurringFrequency.MONTHLY,
            today = LocalDate.of(2026, 2, 1),
            scheduleWeekday = null,
            scheduleMonthDay = 30,
        )

        assertEquals(LocalDate.of(2026, 2, 28), next)
    }
}
