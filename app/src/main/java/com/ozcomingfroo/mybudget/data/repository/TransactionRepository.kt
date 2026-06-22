package com.ozcomingfroo.mybudget.data.repository

import androidx.room.withTransaction
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.dao.BudgetBookDao
import com.ozcomingfroo.mybudget.data.local.dao.TransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.local.query.CategoryAmountSummary
import com.ozcomingfroo.mybudget.data.local.query.MonthlyAmountSummary
import com.ozcomingfroo.mybudget.widget.BalanceWidgetUpdateNotifier
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TransactionRepository @Inject constructor(
    private val database: MyBudgetDatabase,
    private val transactionDao: TransactionDao,
    private val budgetBookDao: BudgetBookDao,
    private val clock: Clock,
    private val widgetUpdateNotifier: BalanceWidgetUpdateNotifier,
) {
    fun observeForBudgetBook(budgetBookId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeForBudgetBook(budgetBookId)

    suspend fun insert(transaction: TransactionEntity): Long {
        val id = database.withTransaction {
            val insertedId = transactionDao.insert(transaction)
            applyTransactionImpact(transaction, multiplier = 1)
            insertedId
        }
        widgetUpdateNotifier.notifyWidgetsChanged()
        return id
    }

    suspend fun update(transaction: TransactionEntity) {
        database.withTransaction {
            val existing = transactionDao.getById(transaction.id) ?: return@withTransaction
            applyTransactionImpact(existing, multiplier = -1)
            transactionDao.update(transaction)
            applyTransactionImpact(transaction, multiplier = 1)
        }
        widgetUpdateNotifier.notifyWidgetsChanged()
    }

    suspend fun delete(transaction: TransactionEntity) {
        database.withTransaction {
            val existing = transactionDao.getById(transaction.id) ?: transaction
            transactionDao.delete(transaction)
            applyTransactionImpact(existing, multiplier = -1)
        }
        widgetUpdateNotifier.notifyWidgetsChanged()
    }

    suspend fun totalByType(
        budgetBookId: Long,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Long = transactionDao.totalByType(
        budgetBookId = budgetBookId,
        type = type,
        startDateTime = startDate.atStartOfDay(),
        endExclusiveDateTime = endDate.plusDays(1).atStartOfDay(),
    )

    suspend fun currentBalance(budgetBookId: Long): Long =
        transactionDao.currentBalance(budgetBookId)

    suspend fun totalsByCategory(
        budgetBookId: Long,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<CategoryAmountSummary> =
        transactionDao.totalsByCategory(
            budgetBookId = budgetBookId,
            type = type,
            startDateTime = startDate.atStartOfDay(),
            endExclusiveDateTime = endDate.plusDays(1).atStartOfDay(),
        )

    suspend fun monthlyTotals(
        budgetBookId: Long,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<MonthlyAmountSummary> =
        transactionDao.monthlyTotals(
            budgetBookId = budgetBookId,
            type = type,
            startDateTime = startDate.atStartOfDay(),
            endExclusiveDateTime = endDate.plusDays(1).atStartOfDay(),
        )

    suspend fun addCachedTotalsForInsertedTransactions(transactions: List<TransactionEntity>) {
        transactions.groupBy { it.budgetBookId }.forEach { (budgetBookId, budgetBookTransactions) ->
            val incomeDelta = budgetBookTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountMinor }
            val expenseDelta = budgetBookTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountMinor }
            if (incomeDelta != 0L || expenseDelta != 0L) {
                budgetBookDao.updateTotals(
                    id = budgetBookId,
                    incomeDelta = incomeDelta,
                    expenseDelta = expenseDelta,
                    updatedAt = clock.instant(),
                )
            }
        }
    }

    private suspend fun applyTransactionImpact(transaction: TransactionEntity, multiplier: Int) {
        val signedAmount = transaction.amountMinor * multiplier
        val incomeDelta = if (transaction.type == TransactionType.INCOME) signedAmount else 0L
        val expenseDelta = if (transaction.type == TransactionType.EXPENSE) signedAmount else 0L
        budgetBookDao.updateTotals(
            id = transaction.budgetBookId,
            incomeDelta = incomeDelta,
            expenseDelta = expenseDelta,
            updatedAt = clock.instant(),
        )
    }
}
