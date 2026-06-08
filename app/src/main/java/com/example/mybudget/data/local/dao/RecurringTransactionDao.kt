package com.example.mybudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mybudget.data.local.entity.RecurringTransactionEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query(
        """
        SELECT * FROM recurring_transactions
        WHERE budget_book_id = :budgetBookId
        ORDER BY is_active DESC, next_run_date, title
        """,
    )
    fun observeForBudgetBook(budgetBookId: Long): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query(
        """
        SELECT * FROM recurring_transactions
        WHERE is_active = 1
        AND next_run_date <= :today
        AND (end_date IS NULL OR next_run_date <= end_date)
        ORDER BY next_run_date, id
        """,
    )
    suspend fun getDue(today: LocalDate): List<RecurringTransactionEntity>

    @Insert
    suspend fun insert(recurringTransaction: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurringTransaction: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)
}
