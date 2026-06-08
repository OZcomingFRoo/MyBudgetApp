package com.example.mybudget.data.repository

import com.example.mybudget.data.local.dao.CategoryDao
import com.example.mybudget.data.local.entity.CategoryEntity
import com.example.mybudget.data.local.model.CategoryType
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val clock: Clock,
) {
    fun observeActiveForBudgetBook(budgetBookId: Long): Flow<List<CategoryEntity>> =
        categoryDao.observeActiveForBudgetBook(budgetBookId)

    fun observeActiveByType(
        budgetBookId: Long,
        type: CategoryType,
    ): Flow<List<CategoryEntity>> = categoryDao.observeActiveByType(budgetBookId, type)

    suspend fun insert(category: CategoryEntity): Long =
        categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) =
        categoryDao.update(category.copy(updatedAt = clock.instant()))

    suspend fun archive(id: Long) {
        val now = clock.instant()
        categoryDao.archive(id = id, archivedAt = now, updatedAt = now)
    }
}
