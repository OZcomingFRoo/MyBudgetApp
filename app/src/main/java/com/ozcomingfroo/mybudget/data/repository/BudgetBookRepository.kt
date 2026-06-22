package com.ozcomingfroo.mybudget.data.repository

import androidx.room.withTransaction
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.local.dao.BudgetBookDao
import com.ozcomingfroo.mybudget.data.local.dao.CategoryDao
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.widget.BalanceWidgetUpdateNotifier
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
    private val widgetUpdateNotifier: BalanceWidgetUpdateNotifier? = null,
    private val clock: Clock,
) {
    fun observeActiveBudgetBooks(): Flow<List<BudgetBookEntity>> =
        budgetBookDao.observeActiveBudgetBooks()

    fun observeArchivedBudgetBooks(): Flow<List<BudgetBookEntity>> =
        budgetBookDao.observeArchivedBudgetBooks()

    suspend fun updateBudgetBookDetails(id: Long, title: String, description: String?) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val existing = budgetBookDao.getById(id) ?: return
        budgetBookDao.update(
            existing.copy(
                title = trimmedTitle,
                description = description?.trim()?.ifBlank { null },
                updatedAt = clock.instant(),
            ),
        )
        widgetUpdateNotifier?.notifyWidgetsChanged()
    }

    suspend fun renameBudgetBook(id: Long, title: String) {
        val existing = budgetBookDao.getById(id) ?: return
        updateBudgetBookDetails(id = id, title = title, description = existing.description)
    }

    suspend fun createBudgetBook(
        title: String,
        description: String? = null,
        selectAfterCreate: Boolean = true,
        starterCategoryTitles: List<StarterCategoryTitle> = StarterCategories.englishTitles(),
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
            categoryDao.insertAll(
                StarterCategories.createForBudgetBook(
                    budgetBookId = budgetBookId,
                    now = now,
                    titles = starterCategoryTitles,
                ),
            )
            budgetBookId
        }
        if (selectAfterCreate) {
            appPreferencesRepository.setSelectedBudgetBookId(id)
        } else {
            widgetUpdateNotifier?.notifyWidgetsChanged()
        }
        return id
    }

    suspend fun localizeStarterCategories(
        budgetBookId: Long,
        starterCategoryTitles: List<StarterCategoryTitle>,
    ) {
        val now = clock.instant()
        database.withTransaction {
            categoryDao.getForBudgetBook(budgetBookId)
                .mapNotNull { category ->
                    StarterCategories.matchingRelocalizedCategory(
                        category = category,
                        localizedTitles = starterCategoryTitles,
                        now = now,
                    )
                }
                .forEach { categoryDao.update(it) }
        }
    }

    suspend fun archiveBudgetBook(id: Long): Boolean {
        val selectedBudgetBookId = appPreferencesRepository.getPreferences().selectedBudgetBookId
        return database.withTransaction {
            if (!canArchiveBudgetBook(id = id, selectedBudgetBookId = selectedBudgetBookId)) {
                false
            } else {
                val now = clock.instant()
                budgetBookDao.archive(id = id, archivedAt = now, updatedAt = now)
                widgetUpdateNotifier?.notifyWidgetsChanged()
                true
            }
        }
    }

    suspend fun deleteBudgetBookPermanently(id: Long): Boolean {
        val selectedBudgetBookId = appPreferencesRepository.getPreferences().selectedBudgetBookId
        return database.withTransaction {
            if (!canDeleteBudgetBook(id = id, selectedBudgetBookId = selectedBudgetBookId)) {
                false
            } else {
                val existing = budgetBookDao.getById(id) ?: return@withTransaction false
                budgetBookDao.delete(existing)
                widgetUpdateNotifier?.notifyWidgetsChanged()
                true
            }
        }
    }

    suspend fun restoreBudgetBook(id: Long): Boolean {
        val restored = budgetBookDao.restore(id = id, updatedAt = clock.instant()) > 0
        if (restored) {
            widgetUpdateNotifier?.notifyWidgetsChanged()
        }
        return restored
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

    private suspend fun canArchiveBudgetBook(id: Long, selectedBudgetBookId: Long?): Boolean {
        if (selectedBudgetBookId == id) return false
        val existing = budgetBookDao.getById(id) ?: return false
        if (existing.archivedAt != null) return false

        return budgetBookDao.activeCount() > 1
    }

    private suspend fun canDeleteBudgetBook(id: Long, selectedBudgetBookId: Long?): Boolean {
        if (selectedBudgetBookId == id) return false
        val existing = budgetBookDao.getById(id) ?: return false
        return existing.archivedAt != null || budgetBookDao.activeCount() > 1
    }
}
