package com.ozcomingfroo.mybudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetBookDao {
    @Query("SELECT * FROM budget_books WHERE archived_at IS NULL ORDER BY title")
    fun observeActiveBudgetBooks(): Flow<List<BudgetBookEntity>>

    @Query("SELECT * FROM budget_books WHERE archived_at IS NOT NULL ORDER BY title")
    fun observeArchivedBudgetBooks(): Flow<List<BudgetBookEntity>>

    @Query("SELECT * FROM budget_books WHERE id = :id")
    suspend fun getById(id: Long): BudgetBookEntity?

    @Query("SELECT * FROM budget_books ORDER BY id LIMIT 1")
    suspend fun getFirst(): BudgetBookEntity?

    @Query("SELECT * FROM budget_books WHERE archived_at IS NULL ORDER BY title LIMIT 1")
    suspend fun getFirstActive(): BudgetBookEntity?

    @Query("SELECT COUNT(*) FROM budget_books")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM budget_books WHERE archived_at IS NULL")
    suspend fun activeCount(): Int

    @Insert
    suspend fun insert(budgetBook: BudgetBookEntity): Long

    @Update
    suspend fun update(budgetBook: BudgetBookEntity)

    @Delete
    suspend fun delete(budgetBook: BudgetBookEntity)

    @Query("UPDATE budget_books SET archived_at = :archivedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, archivedAt: Instant, updatedAt: Instant)

    @Query("UPDATE budget_books SET archived_at = NULL, updated_at = :updatedAt WHERE id = :id AND archived_at IS NOT NULL")
    suspend fun restore(id: Long, updatedAt: Instant): Int

    @Query(
        """
        UPDATE budget_books
        SET total_income_minor = total_income_minor + :incomeDelta,
            total_expense_minor = total_expense_minor + :expenseDelta,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateTotals(
        id: Long,
        incomeDelta: Long,
        expenseDelta: Long,
        updatedAt: Instant,
    )
}
