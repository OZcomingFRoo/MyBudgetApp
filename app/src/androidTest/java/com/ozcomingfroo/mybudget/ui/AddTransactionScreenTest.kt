package com.ozcomingfroo.mybudget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferences
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.data.preferences.DefaultTransactionType
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddTransactionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun saveButtonDisablesWhileSavingAndPreventsDuplicateInserts() {
        val insertStarted = CompletableDeferred<Unit>()
        val finishInsert = CompletableDeferred<Unit>()
        val insertCount = AtomicInteger(0)
        val navigationCount = AtomicInteger(0)

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    preferences = testPreferences(),
                    insertTransaction = { transaction: TransactionEntity ->
                        insertCount.incrementAndGet()
                        assertEquals(1_000L, transaction.amountMinor)
                        assertEquals(CategoryId, transaction.categoryId)
                        assertEquals(LocalDateTime.of(2026, 6, 16, 10, 30), transaction.occurredAt)
                        insertStarted.complete(Unit)
                        finishInsert.await()
                        1L
                    },
                    clock = TestClock,
                    onTransactionSaved = { navigationCount.incrementAndGet() },
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_amount")
            .performTextInput("10")

        composeRule.onNodeWithTag("add_transaction_save")
            .performTouchInput {
                click(center)
                click(center)
                click(center)
            }

        composeRule.waitUntil(timeoutMillis = 5_000) { insertStarted.isCompleted }
        composeRule.onNodeWithTag("add_transaction_save").assertIsNotEnabled()
        composeRule.runOnIdle {
            assertEquals(1, insertCount.get())
            assertEquals(0, navigationCount.get())
        }

        finishInsert.complete(Unit)

        composeRule.waitUntil(timeoutMillis = 5_000) { navigationCount.get() == 1 }
        composeRule.runOnIdle {
            assertEquals(1, insertCount.get())
            assertEquals(1, navigationCount.get())
        }
    }

    @Test
    fun categoryCardSelectionChangesSavedCategory() {
        val savedCategoryId = AtomicLong(0)

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(
                        testCategory(id = CategoryId, title = "Food"),
                        testCategory(id = SecondCategoryId, title = "Transport"),
                    ),
                    preferences = testPreferences(),
                    insertTransaction = { transaction: TransactionEntity ->
                        savedCategoryId.set(transaction.categoryId ?: 0)
                        1L
                    },
                    clock = TestClock,
                    onTransactionSaved = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_amount")
            .performTextInput("5")
        composeRule.onNodeWithTag("add_transaction_category_$SecondCategoryId")
            .performTouchInput { click(center) }
        composeRule.onNodeWithTag("add_transaction_save")
            .performTouchInput { click(center) }

        composeRule.waitUntil(timeoutMillis = 5_000) { savedCategoryId.get() == SecondCategoryId }
    }

    private fun testPreferences() = AppPreferences(
        selectedBudgetBookId = BudgetBookId,
        themeMode = AppThemeMode.DEFAULT,
        languageMode = AppLanguageMode.EN_US,
        hasCompletedOnboarding = true,
        defaultTransactionType = DefaultTransactionType.EXPENSE,
    )

    private fun testCategory(
        id: Long = CategoryId,
        title: String = "Food",
    ) = CategoryEntity(
        id = id,
        budgetBookId = BudgetBookId,
        title = title,
        type = CategoryType.EXPENSE,
        iconName = "restaurant",
        color = "#2E7D32",
        sortOrder = 0,
        createdAt = TestInstant,
        updatedAt = TestInstant,
    )

    private companion object {
        const val BudgetBookId = 1L
        const val CategoryId = 2L
        const val SecondCategoryId = 3L
        val TestInstant: Instant = Instant.parse("2026-06-16T10:30:00Z")
        val TestClock: Clock = Clock.fixed(TestInstant, ZoneOffset.UTC)
    }
}
