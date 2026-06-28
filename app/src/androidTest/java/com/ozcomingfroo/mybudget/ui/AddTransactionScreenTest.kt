package com.ozcomingfroo.mybudget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
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
import org.junit.Assert.assertTrue
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

    @Test
    fun amountInputLimitsDecimalPlacesBeforeSaving() {
        val savedAmountMinor = AtomicLong(0)

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    preferences = testPreferences(),
                    insertTransaction = { transaction: TransactionEntity ->
                        savedAmountMinor.set(transaction.amountMinor)
                        1L
                    },
                    clock = TestClock,
                    onTransactionSaved = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_amount")
            .performTextInput("10.999")
        composeRule.onNodeWithTag("add_transaction_save")
            .performTouchInput { click(center) }

        composeRule.waitUntil(timeoutMillis = 5_000) { savedAmountMinor.get() == 1_099L }
    }

    @Test
    fun cancelButtonVisibilityFollowsDashboardLaunchFlag() {
        val cancelCount = AtomicInteger(0)

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    preferences = testPreferences(),
                    insertTransaction = { 1L },
                    clock = TestClock,
                    showCancelButton = false,
                    onCancel = { cancelCount.incrementAndGet() },
                    onTransactionSaved = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.runOnIdle {
            assertTrue(
                composeRule.onAllNodesWithTag("add_transaction_cancel")
                    .fetchSemanticsNodes()
                    .isEmpty(),
            )
        }

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory()),
                    preferences = testPreferences(),
                    insertTransaction = { 1L },
                    clock = TestClock,
                    showCancelButton = true,
                    onCancel = { cancelCount.incrementAndGet() },
                    onTransactionSaved = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_cancel")
            .assertIsDisplayed()
            .performClick()
        composeRule.runOnIdle {
            assertEquals(1, cancelCount.get())
        }
    }

    @Test
    fun initialTransactionTypeOverridesDefaultTypeWhenSaving() {
        val savedType = java.util.concurrent.atomic.AtomicReference<TransactionType>()

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                AddTransactionScreen(
                    selectedBudgetBookId = BudgetBookId,
                    categories = listOf(testCategory(type = CategoryType.INCOME)),
                    preferences = testPreferences(defaultTransactionType = DefaultTransactionType.EXPENSE),
                    insertTransaction = { transaction: TransactionEntity ->
                        savedType.set(transaction.type)
                        1L
                    },
                    clock = TestClock,
                    initialTransactionType = TransactionType.INCOME,
                    onTransactionSaved = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_amount")
            .performTextInput("25")
        composeRule.onNodeWithTag("add_transaction_save")
            .performTouchInput { click(center) }

        composeRule.waitUntil(timeoutMillis = 5_000) { savedType.get() == TransactionType.INCOME }
    }

    @Test
    fun editTransactionSheetUpdatesExistingTransaction() {
        val updatedId = AtomicLong(0)
        val originalCreatedAt = Instant.parse("2026-06-01T08:00:00Z")
        val transaction = TransactionEntity(
            id = 44L,
            budgetBookId = BudgetBookId,
            categoryId = CategoryId,
            type = TransactionType.EXPENSE,
            amountMinor = 1_234L,
            title = "Lunch",
            note = "Cafe",
            occurredAt = LocalDateTime.of(2026, 6, 15, 9, 45),
            createdAt = originalCreatedAt,
            updatedAt = originalCreatedAt,
        )

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                EditTransactionSheet(
                    transaction = transaction,
                    categories = listOf(testCategory()),
                    clock = TestClock,
                    onDismiss = {},
                    onTransactionUpdated = { updated ->
                        updatedId.set(updated.id)
                        assertEquals(transaction.budgetBookId, updated.budgetBookId)
                        assertEquals(transaction.createdAt, updated.createdAt)
                        assertEquals(TestInstant, updated.updatedAt)
                        assertEquals(transaction.categoryId, updated.categoryId)
                    },
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithTag("add_transaction_save")
            .performTouchInput { click(center) }

        composeRule.waitUntil(timeoutMillis = 5_000) { updatedId.get() == transaction.id }
    }

    private fun testPreferences(
        defaultTransactionType: DefaultTransactionType = DefaultTransactionType.EXPENSE,
    ) = AppPreferences(
        selectedBudgetBookId = BudgetBookId,
        themeMode = AppThemeMode.DEFAULT,
        languageMode = AppLanguageMode.EN_US,
        hasCompletedOnboarding = true,
        defaultTransactionType = defaultTransactionType,
    )

    private fun testCategory(
        id: Long = CategoryId,
        title: String = "Food",
        type: CategoryType = CategoryType.EXPENSE,
    ) = CategoryEntity(
        id = id,
        budgetBookId = BudgetBookId,
        title = title,
        type = type,
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
