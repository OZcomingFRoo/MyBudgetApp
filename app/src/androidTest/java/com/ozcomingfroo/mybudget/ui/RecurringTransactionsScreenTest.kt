package com.ozcomingfroo.mybudget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ozcomingfroo.mybudget.data.local.dao.RecurringTransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecurringTransactionsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addSheetSavesValidRecurringTransaction() {
        val savedAmountMinor = AtomicLong(0)
        val savedCategoryId = AtomicLong(0)

        composeRule.setContent {
            MyBudgetTheme {
                RecurringTransactionEditorSheet(
                    rule = null,
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    clock = TestClock,
                    onDismiss = {},
                    onSave = { rule ->
                        savedAmountMinor.set(rule.amountMinor)
                        savedCategoryId.set(rule.categoryId ?: 0L)
                        assertEquals(BudgetBookId, rule.budgetBookId)
                        assertEquals(TransactionType.EXPENSE, rule.type)
                        assertEquals(RecurringFrequency.MONTHLY, rule.frequency)
                        assertEquals(1, rule.interval)
                        assertEquals(LocalDate.of(2026, 6, 16), rule.startDate)
                        assertEquals(LocalDate.of(2026, 6, 16), rule.nextRunDate)
                        assertEquals(TestInstant, rule.createdAt)
                    },
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("recurring_amount").performTextInput("25")
        composeRule.onNodeWithTag("recurring_save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { savedAmountMinor.get() == 2_500L }
        composeRule.runOnIdle {
            assertEquals(CategoryId, savedCategoryId.get())
        }
    }

    @Test
    fun editSheetPreservesIdentityAndCreatedAt() {
        val savedId = AtomicLong(0)
        val originalCreatedAt = Instant.parse("2026-06-01T08:00:00Z")
        val rule = testRule(
            id = RuleId,
            createdAt = originalCreatedAt,
            updatedAt = originalCreatedAt,
        )

        composeRule.setContent {
            MyBudgetTheme {
                RecurringTransactionEditorSheet(
                    rule = rule,
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    clock = TestClock,
                    onDismiss = {},
                    onSave = { updated ->
                        savedId.set(updated.id)
                        assertEquals(rule.budgetBookId, updated.budgetBookId)
                        assertEquals(rule.createdAt, updated.createdAt)
                        assertEquals(rule.updatedAt, updated.updatedAt)
                        assertEquals(rule.lastRunDate, updated.lastRunDate)
                    },
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("recurring_save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { savedId.get() == RuleId }
    }

    @Test
    fun deleteRequiresConfirmationBeforeDeletingRule() {
        val deletedId = AtomicLong(0)
        val fakeDao = object : RecurringTransactionDao {
            override fun observeForBudgetBook(budgetBookId: Long): Flow<List<RecurringTransactionEntity>> =
                flowOf(listOf(testRule()))

            override suspend fun getById(id: Long): RecurringTransactionEntity? = null

            override suspend fun getDue(today: LocalDate): List<RecurringTransactionEntity> = emptyList()

            override suspend fun insert(recurringTransaction: RecurringTransactionEntity): Long = 1L

            override suspend fun update(recurringTransaction: RecurringTransactionEntity) = Unit

            override suspend fun delete(recurringTransaction: RecurringTransactionEntity) {
                deletedId.set(recurringTransaction.id)
            }

            override suspend fun deleteByCategoryId(categoryId: Long) = Unit
        }
        val repository = RecurringTransactionRepository(
            recurringTransactionDao = fakeDao,
            clock = TestClock,
        )

        composeRule.setContent {
            MyBudgetTheme {
                RecurringTransactionsScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    recurringTransactionRepository = repository,
                    clock = TestClock,
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("recurring_delete_$RuleId").performClick()
        composeRule.runOnIdle {
            assertEquals(0L, deletedId.get())
        }
        composeRule.onNodeWithTag("recurring_delete_confirm").assertIsDisplayed()
        composeRule.onNodeWithTag("recurring_delete_confirm").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { deletedId.get() == RuleId }
    }

    private fun testCategory() = CategoryEntity(
        id = CategoryId,
        budgetBookId = BudgetBookId,
        title = "Rent",
        type = CategoryType.EXPENSE,
        iconName = "home",
        color = "#334155",
        sortOrder = 0,
        createdAt = TestInstant,
        updatedAt = TestInstant,
    )

    private fun testRule(
        id: Long = RuleId,
        createdAt: Instant = TestInstant,
        updatedAt: Instant = TestInstant,
    ) = RecurringTransactionEntity(
        id = id,
        budgetBookId = BudgetBookId,
        categoryId = CategoryId,
        type = TransactionType.EXPENSE,
        amountMinor = 120_000,
        title = "Rent",
        frequency = RecurringFrequency.MONTHLY,
        interval = 1,
        startDate = LocalDate.of(2026, 6, 1),
        nextRunDate = LocalDate.of(2026, 7, 1),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private companion object {
        const val BudgetBookId = 1L
        const val CategoryId = 2L
        const val RuleId = 7L
        val TestInstant: Instant = Instant.parse("2026-06-16T10:30:00Z")
        val TestClock: Clock = Clock.fixed(TestInstant, ZoneOffset.UTC)
    }
}
