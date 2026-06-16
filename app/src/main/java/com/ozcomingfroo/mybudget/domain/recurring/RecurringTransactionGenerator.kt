package com.ozcomingfroo.mybudget.domain.recurring

import androidx.room.withTransaction
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.dao.RecurringTransactionDao
import com.ozcomingfroo.mybudget.data.local.dao.TransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionGenerator @Inject constructor(
    private val database: MyBudgetDatabase,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao,
    private val clock: Clock,
) {
    suspend fun generateDue(today: LocalDate = LocalDate.now(clock)): Int {
        val dueRules = recurringTransactionDao.getDue(today)
        if (dueRules.isEmpty()) return 0

        return database.withTransaction {
            dueRules.sumOf { rule -> generateDueForRule(rule, today) }
        }
    }

    private suspend fun generateDueForRule(
        rule: RecurringTransactionEntity,
        today: LocalDate,
    ): Int {
        require(rule.interval >= 1) { "Recurring transaction interval must be at least 1." }

        val generatedTransactions = mutableListOf<TransactionEntity>()
        var dueDate = rule.nextRunDate
        var lastGeneratedDate: LocalDate? = null
        var nextRunDate = dueDate

        while (dueDate <= today && (rule.endDate == null || dueDate <= rule.endDate)) {
            val now = clock.instant()
            generatedTransactions += TransactionEntity(
                budgetBookId = rule.budgetBookId,
                categoryId = rule.categoryId,
                recurringTransactionId = rule.id,
                type = rule.type,
                amountMinor = rule.amountMinor,
                title = rule.title,
                note = rule.note,
                occurredAt = dueDate.atStartOfDay(),
                createdAt = now,
                updatedAt = now,
            )
            lastGeneratedDate = dueDate
            nextRunDate = RecurrenceCalculator.nextDate(rule, dueDate)
            dueDate = nextRunDate
        }

        if (generatedTransactions.isNotEmpty()) {
            transactionDao.insertAll(generatedTransactions)
            recurringTransactionDao.update(
                rule.copy(
                    lastRunDate = lastGeneratedDate,
                    nextRunDate = nextRunDate,
                    updatedAt = clock.instant(),
                ),
            )
        }

        return generatedTransactions.size
    }
}
