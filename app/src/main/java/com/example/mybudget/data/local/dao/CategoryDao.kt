package com.example.mybudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mybudget.data.local.entity.CategoryEntity
import com.example.mybudget.data.local.model.CategoryType
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT * FROM categories
        WHERE budget_book_id = :budgetBookId AND archived_at IS NULL
        ORDER BY sort_order, title
        """,
    )
    fun observeActiveForBudgetBook(budgetBookId: Long): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE budget_book_id = :budgetBookId
        AND archived_at IS NULL
        AND (type = :type OR type = 'BOTH')
        ORDER BY sort_order, title
        """,
    )
    fun observeActiveByType(budgetBookId: Long, type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("UPDATE categories SET archived_at = :archivedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, archivedAt: Instant, updatedAt: Instant)
}
