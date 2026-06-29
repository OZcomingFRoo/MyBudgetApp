package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.dao.RecurringTransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.domain.recurring.RecurringSchedule
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val clock: Clock,
) {
    fun observeForBudgetBook(budgetBookId: Long): Flow<List<RecurringTransactionEntity>> =
        recurringTransactionDao.observeForBudgetBook(budgetBookId)

    suspend fun insert(rule: RecurringTransactionEntity): Long {
        validate(rule)
        return recurringTransactionDao.insert(rule.copy(updatedAt = clock.instant()))
    }

    suspend fun update(rule: RecurringTransactionEntity) {
        validate(rule)
        recurringTransactionDao.update(rule.copy(updatedAt = clock.instant()))
    }

    suspend fun delete(rule: RecurringTransactionEntity) {
        recurringTransactionDao.delete(rule)
    }

    private fun validate(rule: RecurringTransactionEntity) {
        require(rule.interval >= 1) { "Recurring transaction interval must be at least 1." }
        require(rule.amountMinor >= 0) { "Recurring transaction amount cannot be negative." }
        require(rule.nextRunDate >= rule.startDate) { "Next run date cannot be before start date." }
        require(rule.endDate == null || rule.endDate >= rule.startDate) {
            "End date cannot be before start date."
        }
        require(rule.scheduleWeekday == null || rule.scheduleWeekday in 1..7) {
            "Weekly schedule weekday must be between 1 and 7."
        }
        require(rule.scheduleMonthDay == null || rule.scheduleMonthDay in RecurringSchedule.MinMonthDay..RecurringSchedule.MaxMonthDay) {
            "Monthly schedule day must be between 1 and 30."
        }
        require(rule.frequency != RecurringFrequency.WEEKLY || rule.scheduleWeekday != null) {
            "Weekly recurring transactions must store a schedule weekday."
        }
        require(rule.frequency != RecurringFrequency.MONTHLY || rule.scheduleMonthDay != null) {
            "Monthly recurring transactions must store a schedule month day."
        }
    }
}
