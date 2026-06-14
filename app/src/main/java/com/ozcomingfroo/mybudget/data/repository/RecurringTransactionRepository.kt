package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.dao.RecurringTransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
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

    private fun validate(rule: RecurringTransactionEntity) {
        require(rule.interval >= 1) { "Recurring transaction interval must be at least 1." }
        require(rule.amountMinor >= 0) { "Recurring transaction amount cannot be negative." }
        require(rule.nextRunDate >= rule.startDate) { "Next run date cannot be before start date." }
        require(rule.endDate == null || rule.endDate >= rule.startDate) {
            "End date cannot be before start date."
        }
    }
}
