package com.ozcomingfroo.mybudget.data.repository

import com.ozcomingfroo.mybudget.data.local.dao.TransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.local.query.CategoryAmountSummary
import com.ozcomingfroo.mybudget.data.local.query.MonthlyAmountSummary
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    fun observeForBudgetBook(budgetBookId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeForBudgetBook(budgetBookId)

    suspend fun insert(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) =
        transactionDao.delete(transaction)

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
}
