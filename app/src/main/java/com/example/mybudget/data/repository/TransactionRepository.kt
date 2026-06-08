package com.example.mybudget.data.repository

import com.example.mybudget.data.local.dao.TransactionDao
import com.example.mybudget.data.local.entity.TransactionEntity
import com.example.mybudget.data.local.model.TransactionType
import com.example.mybudget.data.local.query.CategoryAmountSummary
import com.example.mybudget.data.local.query.MonthlyAmountSummary
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
    ): Long = transactionDao.totalByType(budgetBookId, type, startDate, endDate)

    suspend fun currentBalance(budgetBookId: Long): Long =
        transactionDao.currentBalance(budgetBookId)

    suspend fun totalsByCategory(
        budgetBookId: Long,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<CategoryAmountSummary> =
        transactionDao.totalsByCategory(budgetBookId, type, startDate, endDate)

    suspend fun monthlyTotals(
        budgetBookId: Long,
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<MonthlyAmountSummary> =
        transactionDao.monthlyTotals(budgetBookId, type, startDate, endDate)
}
