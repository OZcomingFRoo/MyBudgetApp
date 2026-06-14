package com.ozcomingfroo.mybudget.data.repository

import androidx.room.withTransaction
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.dao.BudgetBookDao
import com.ozcomingfroo.mybudget.data.local.dao.CategoryDao
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BudgetBookRepository @Inject constructor(
    private val database: MyBudgetDatabase,
    private val budgetBookDao: BudgetBookDao,
    private val categoryDao: CategoryDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val clock: Clock,
) {
    fun observeActiveBudgetBooks(): Flow<List<BudgetBookEntity>> =
        budgetBookDao.observeActiveBudgetBooks()

    suspend fun renameBudgetBook(id: Long, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val existing = budgetBookDao.getById(id) ?: return
        budgetBookDao.update(
            existing.copy(
                title = trimmedTitle,
                updatedAt = clock.instant(),
            ),
        )
    }

    suspend fun createBudgetBook(
        title: String,
        description: String? = null,
        selectAfterCreate: Boolean = true,
    ): Long {
        val now = clock.instant()
        val id = database.withTransaction {
            val budgetBookId = budgetBookDao.insert(
                BudgetBookEntity(
                    title = title.trim(),
                    description = description?.trim()?.ifBlank { null },
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            categoryDao.insertAll(StarterCategories.createForBudgetBook(budgetBookId, now))
            budgetBookId
        }
        if (selectAfterCreate) {
            appPreferencesRepository.setSelectedBudgetBookId(id)
        }
        return id
    }

    suspend fun ensureDefaultBudgetBook(): Long {
        val existing = budgetBookDao.getFirst()
        if (existing != null) {
            if (appPreferencesRepository.getPreferences().selectedBudgetBookId == null) {
                appPreferencesRepository.setSelectedBudgetBookId(existing.id)
            }
            return existing.id
        }
        return createBudgetBook(title = "Personal")
    }
}
