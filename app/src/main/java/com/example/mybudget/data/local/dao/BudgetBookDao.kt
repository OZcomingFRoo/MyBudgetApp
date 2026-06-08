package com.example.mybudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mybudget.data.local.entity.BudgetBookEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetBookDao {
    @Query("SELECT * FROM budget_books WHERE archived_at IS NULL ORDER BY title")
    fun observeActiveBudgetBooks(): Flow<List<BudgetBookEntity>>

    @Query("SELECT * FROM budget_books WHERE id = :id")
    suspend fun getById(id: Long): BudgetBookEntity?

    @Query("SELECT * FROM budget_books ORDER BY id LIMIT 1")
    suspend fun getFirst(): BudgetBookEntity?

    @Query("SELECT COUNT(*) FROM budget_books")
    suspend fun count(): Int

    @Insert
    suspend fun insert(budgetBook: BudgetBookEntity): Long

    @Update
    suspend fun update(budgetBook: BudgetBookEntity)

    @Delete
    suspend fun delete(budgetBook: BudgetBookEntity)

    @Query("UPDATE budget_books SET archived_at = :archivedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, archivedAt: Instant, updatedAt: Instant)
}
