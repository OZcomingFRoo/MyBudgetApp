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
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
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

    private fun testPreferences() = AppPreferences(
        selectedBudgetBookId = BudgetBookId,
        themeMode = AppThemeMode.DEFAULT,
        languageMode = AppLanguageMode.EN_US,
        hasCompletedOnboarding = true,
        defaultTransactionType = DefaultTransactionType.EXPENSE,
    )

    private fun testCategory() = CategoryEntity(
        id = CategoryId,
        budgetBookId = BudgetBookId,
        title = "Food",
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
        val TestInstant: Instant = Instant.parse("2026-06-16T00:00:00Z")
        val TestClock: Clock = Clock.fixed(TestInstant, ZoneOffset.UTC)
    }
}
