package com.ozcomingfroo.mybudget.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode
import com.ozcomingfroo.mybudget.ui.theme.MyBudgetTheme
import java.time.Instant
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoryEditorSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createSheetShowsTwoEqualWidthBottomActions() {
        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                CategoryEditorSheet(
                    category = null,
                    initialType = CategoryType.EXPENSE,
                    onSave = {},
                    onDelete = {},
                    onDismiss = {},
                )
            }
        }

        assertEquals(0, composeRule.onAllNodesWithTag("category_editor_delete").fetchSemanticsNodes().size)
        composeRule.onNodeWithTag("category_editor_cancel").fetchSemanticsNode()
        composeRule.onNodeWithTag("category_editor_save").fetchSemanticsNode()
        assertEqualWidths("category_editor_cancel", "category_editor_save")
    }

    @Test
    fun editSheetShowsThreeEqualWidthBottomActionsIncludingDelete() {
        composeRule.setContent {
            MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                CategoryEditorSheet(
                    category = testCategory(),
                    initialType = CategoryType.EXPENSE,
                    onSave = {},
                    onDelete = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("category_editor_delete").fetchSemanticsNode()
        composeRule.onNodeWithTag("category_editor_cancel").fetchSemanticsNode()
        composeRule.onNodeWithTag("category_editor_save").fetchSemanticsNode()
        assertEqualWidths(
            "category_editor_delete",
            "category_editor_cancel",
            "category_editor_save",
        )
    }

    @Test
    fun hebrewSheetShowsLocalizedLabels() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    CategoryEditorSheet(
                        category = null,
                        initialType = CategoryType.EXPENSE,
                        onSave = {},
                        onDelete = {},
                        onDismiss = {},
                    )
                }
            }
        }

        listOf(
            R.string.create_category,
            R.string.category_name,
            R.string.category_type,
            R.string.category_icon,
            R.string.category_color,
            R.string.cancel,
            R.string.save,
        ).forEach { stringId ->
            composeRule.onNodeWithText(heContext.getString(stringId)).fetchSemanticsNode()
        }
    }

    @Test
    fun categoryTypeSelectorKeepsExpenseThenIncomeOrderInRtl() {
        val heContext = localizedContext(Locale.forLanguageTag("he"))

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides heContext,
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MyBudgetTheme(themeMode = AppThemeMode.DEFAULT) {
                    CategoryEditorSheet(
                        category = null,
                        initialType = CategoryType.EXPENSE,
                        onSave = {},
                        onDelete = {},
                        onDismiss = {},
                    )
                }
            }
        }

        val expenseLeft = composeRule
            .onNodeWithText(heContext.getString(R.string.expense))
            .getUnclippedBoundsInRoot()
            .left
        val incomeLeft = composeRule
            .onNodeWithText(heContext.getString(R.string.income))
            .getUnclippedBoundsInRoot()
            .left

        assertTrue(expenseLeft < incomeLeft)
    }

    private fun assertEqualWidths(vararg tags: String) {
        val widths = tags.map { tag ->
            val bounds = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            (bounds.right - bounds.left).value
        }
        widths.drop(1).forEach { width ->
            assertEquals(widths.first(), width, 1f)
        }
    }

    private fun localizedContext(locale: Locale): Context {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    private fun testCategory() = CategoryEntity(
        id = 2L,
        budgetBookId = 1L,
        title = "Food",
        type = CategoryType.EXPENSE,
        iconName = "restaurant",
        color = "#2E7D32",
        sortOrder = 0,
        createdAt = TestInstant,
        updatedAt = TestInstant,
    )

    private companion object {
        val TestInstant: Instant = Instant.parse("2026-06-16T00:00:00Z")
    }
}
