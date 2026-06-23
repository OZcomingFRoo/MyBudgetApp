package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryKey
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryTitle
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Instant
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createAccountSaveDisablesWhileSavingAndPreventsDuplicateCreates() {
        val createStarted = CompletableDeferred<Unit>()
        val finishCreate = CompletableDeferred<Unit>()
        val createCount = AtomicInteger(0)
        val navigationCount = AtomicInteger(0)

        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                CreateAccountScreen(
                    initialSeedLanguageMode = AppLanguageMode.EN_US,
                    createBudgetBook = { title, description, _ ->
                        createCount.incrementAndGet()
                        assertEquals("Work", title)
                        assertEquals("", description)
                        createStarted.complete(Unit)
                        finishCreate.await()
                        7L
                    },
                    snackbarHostState = SnackbarHostState(),
                    onCreated = { navigationCount.incrementAndGet() },
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithTag("create_account_title")
            .performTextInput("Work")
        composeRule.onNodeWithTag("create_account_save")
            .performTouchInput {
                click(center)
                click(center)
                click(center)
            }

        composeRule.waitUntil(timeoutMillis = 5_000) { createStarted.isCompleted }
        composeRule.onNodeWithTag("create_account_save").assertIsNotEnabled()
        composeRule.runOnIdle {
            assertEquals(1, createCount.get())
            assertEquals(0, navigationCount.get())
        }

        finishCreate.complete(Unit)

        composeRule.waitUntil(timeoutMillis = 5_000) { navigationCount.get() == 1 }
        composeRule.runOnIdle {
            assertEquals(1, createCount.get())
            assertEquals(1, navigationCount.get())
        }
    }

    @Test
    fun createAccountSeedLanguageControlsStarterTitlesOnly() {
        val englishContext = localizedContext(Locale.US)
        val hebrewContext = localizedContext(Locale.forLanguageTag("he"))
        val capturedTitles = AtomicReference<List<StarterCategoryTitle>>()

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides englishContext,
                LocalLayoutDirection provides LayoutDirection.Ltr,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    CreateAccountScreen(
                        initialSeedLanguageMode = AppLanguageMode.EN_US,
                        createBudgetBook = { _, _, starterCategoryTitles ->
                            capturedTitles.set(starterCategoryTitles)
                            7L
                        },
                        snackbarHostState = SnackbarHostState(),
                        onCreated = {},
                        onCancel = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag("create_account_seed_language_HE")
            .performClick()
        assertTrue(
            composeRule
                .onAllNodesWithText(englishContext.getString(R.string.create_account_cancel))
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
        assertTrue(
            composeRule
                .onAllNodesWithText(englishContext.getString(R.string.create_account_submit))
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
        composeRule.onNodeWithTag("create_account_title")
            .performTextInput("Hebrew Categories")
        composeRule.onNodeWithTag("create_account_save")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { capturedTitles.get() != null }
        val titles = capturedTitles.get().associate { it.key to it.title }
        val hebrewGroceries = hebrewContext.getString(R.string.starter_category_groceries)

        assertEquals(hebrewGroceries, titles[StarterCategoryKey.GROCERIES])
        assertNotEquals("Groceries", titles[StarterCategoryKey.GROCERIES])
    }

    @Test
    fun createAccountShowsHebrewActionLabels() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    CreateAccountScreen(
                        initialSeedLanguageMode = AppLanguageMode.HE,
                        createBudgetBook = { _, _, _ -> 7L },
                        snackbarHostState = SnackbarHostState(),
                        onCreated = {},
                        onCancel = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(heContext.getString(R.string.create_account_cancel))
            .fetchSemanticsNode()
        composeRule.onNodeWithTag("create_account_cancel")
            .assertHeightIsAtLeast(56.dp)
        composeRule.onNodeWithTag("create_account_save")
            .assertHeightIsAtLeast(56.dp)
        assertTrue(
            composeRule
                .onAllNodesWithText(heContext.getString(R.string.create_account_submit))
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }

    @Test
    fun accountEditorShowsHebrewLabelsAndBlockedRemovalMessage() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    AccountEditorSheet(
                        budgetBook = testBudgetBook(),
                        canRemove = false,
                        strings = accountEditorStrings(heContext),
                        onSave = { _, _ -> },
                        onArchive = {},
                        onDeletePermanently = {},
                        onDismiss = {},
                    )
                }
            }
        }

        listOf(
            R.string.edit_account,
            R.string.budget_book_name,
            R.string.budget_book_description,
            R.string.archive,
            R.string.delete_account_permanently,
            R.string.cancel,
            R.string.save,
            R.string.account_delete_blocked,
        ).forEach { stringId ->
            composeRule.onNodeWithText(heContext.getString(stringId)).fetchSemanticsNode()
        }
    }

    @Test
    fun accountEditorShowsHebrewArchiveDialog() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    AccountEditorSheet(
                        budgetBook = testBudgetBook(),
                        canRemove = true,
                        strings = accountEditorStrings(heContext),
                        onSave = { _, _ -> },
                        onArchive = {},
                        onDeletePermanently = {},
                        onDismiss = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(heContext.getString(R.string.archive))
            .performClick()
        composeRule.onNodeWithText(heContext.getString(R.string.archive_account_title))
            .fetchSemanticsNode()
        composeRule.onNodeWithText(heContext.getString(R.string.archive_account_message, "Work"))
            .fetchSemanticsNode()
        assertTrue(composeRule.onAllNodesWithText("Archive account?").fetchSemanticsNodes().isEmpty())
        assertTrue(
            composeRule
                .onAllNodesWithText(
                    "Archive Work? Its data stays saved, but the account will be hidden from active accounts.",
                )
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun accountEditorShowsHebrewPermanentDeleteDialog() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    AccountEditorSheet(
                        budgetBook = testBudgetBook(),
                        canRemove = true,
                        strings = accountEditorStrings(heContext),
                        onSave = { _, _ -> },
                        onArchive = {},
                        onDeletePermanently = {},
                        onDismiss = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(heContext.getString(R.string.delete_account_permanently))
            .performClick()
        composeRule.onNodeWithText(heContext.getString(R.string.delete_account_permanently_title))
            .fetchSemanticsNode()
        composeRule.onNodeWithText(heContext.getString(R.string.delete_account_permanently_message, "Work"))
            .fetchSemanticsNode()
        assertTrue(composeRule.onAllNodesWithText("Delete account permanently?").fetchSemanticsNodes().isEmpty())
        assertTrue(
            composeRule
                .onAllNodesWithText(
                    "Delete Work and all of its categories, transactions, and recurring transactions? This cannot be undone.",
                )
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    private fun localizedContext(locale: Locale): Context {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    private fun accountEditorStrings(context: Context) = AccountEditorStrings(
        editAccount = context.getString(R.string.edit_account),
        budgetBookName = context.getString(R.string.budget_book_name),
        budgetBookNameHelper = context.getString(R.string.budget_book_name_helper),
        budgetBookDescription = context.getString(R.string.budget_book_description),
        budgetBookDescriptionHelper = context.getString(R.string.budget_book_description_helper),
        accountDeleteBlocked = context.getString(R.string.account_delete_blocked),
        archive = context.getString(R.string.archive),
        archiveAccountTitle = context.getString(R.string.archive_account_title),
        archiveAccountMessage = context.getString(R.string.archive_account_message, "Work"),
        deleteAccountPermanently = context.getString(R.string.delete_account_permanently),
        deleteAccountPermanentlyTitle = context.getString(R.string.delete_account_permanently_title),
        deleteAccountPermanentlyMessage = context.getString(R.string.delete_account_permanently_message, "Work"),
        cancel = context.getString(R.string.cancel),
        save = context.getString(R.string.save),
    )

    private fun testBudgetBook() = BudgetBookEntity(
        id = 7L,
        title = "Work",
        description = "Client work",
        createdAt = TestInstant,
        updatedAt = TestInstant,
    )

    private companion object {
        val TestInstant: Instant = Instant.parse("2026-06-23T10:00:00Z")
    }
}
