package com.ozcomingfroo.mybudget.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.ozcomingfroo.mybudget.R
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.preferences.AppPreferencesRepository
import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.data.repository.CategoryRepository
import com.ozcomingfroo.mybudget.data.repository.RecurringTransactionRepository
import com.ozcomingfroo.mybudget.data.repository.TransactionRepository
import com.ozcomingfroo.mybudget.widget.BalanceWidgetUpdateNotifier
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyBudgetAppBackNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var database: MyBudgetDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var appPreferencesRepository: AppPreferencesRepository
    private lateinit var budgetBookRepository: BudgetBookRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MyBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStoreFile = File(context.cacheDir, "mybudget_app_back_navigation_${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { dataStoreFile },
        )
        appPreferencesRepository = AppPreferencesRepository(dataStore)
        budgetBookRepository = BudgetBookRepository(
            database = database,
            budgetBookDao = database.budgetBookDao(),
            categoryDao = database.categoryDao(),
            appPreferencesRepository = appPreferencesRepository,
            clock = TestClock,
        )
        categoryRepository = CategoryRepository(
            database = database,
            categoryDao = database.categoryDao(),
            recurringTransactionDao = database.recurringTransactionDao(),
            clock = TestClock,
        )
        transactionRepository = TransactionRepository(
            database = database,
            transactionDao = database.transactionDao(),
            budgetBookDao = database.budgetBookDao(),
            clock = TestClock,
            widgetUpdateNotifier = BalanceWidgetUpdateNotifier(context),
        )
        recurringTransactionRepository = RecurringTransactionRepository(
            recurringTransactionDao = database.recurringTransactionDao(),
            clock = TestClock,
        )

        runBlocking {
            appPreferencesRepository.setLanguageMode(AppLanguageMode.EN_US)
            appPreferencesRepository.setHasCompletedOnboarding(true)
            budgetBookRepository.createBudgetBook(title = "Personal")
        }
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
        dataStoreFile.delete()
    }

    @Test
    fun backClosesOpenDrawer() {
        setAppContent()

        composeRule.onNodeWithContentDescription(context.getString(R.string.menu_button))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText(context.getString(R.string.nav_dashboard))
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(context.getString(R.string.nav_settings))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        assertTrue(
            composeRule.onAllNodesWithText(context.getString(R.string.nav_settings))
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun backFromNonDashboardReturnsToDashboard() {
        setAppContent()

        composeRule.onNodeWithContentDescription(context.getString(R.string.menu_button))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText(context.getString(R.string.nav_dashboard))
            .assertIsDisplayed()
    }

    @Test
    fun backClosesOpenDrawerBeforeLeavingNonDashboardScreen() {
        setAppContent()

        composeRule.onNodeWithContentDescription(context.getString(R.string.menu_button))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.menu_button))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_dashboard))
            .assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText(context.getString(R.string.nav_settings))
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(context.getString(R.string.nav_dashboard))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        assertTrue(
            composeRule.onAllNodesWithText(context.getString(R.string.nav_dashboard))
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun dashboardDrawerItemReturnsFromDashboardLaunchedAddTransaction() {
        setAppContent()

        composeRule.onNodeWithText(context.getString(R.string.add_expense))
            .performClick()
        composeRule.onNodeWithContentDescription(context.getString(R.string.menu_button))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.nav_dashboard))
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.add_expense))
            .assertIsDisplayed()
    }

    @Test
    fun cancelReturnsFromDashboardLaunchedAddTransaction() {
        setAppContent()

        composeRule.onNodeWithText(context.getString(R.string.add_income))
            .performClick()
        composeRule.onNodeWithTag("add_transaction_cancel")
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.add_income))
            .assertIsDisplayed()
    }

    private fun setAppContent() {
        composeRule.setContent {
            MyBudgetApp(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                budgetBookRepository = budgetBookRepository,
                appPreferencesRepository = appPreferencesRepository,
                clock = TestClock,
            )
        }
    }

    private companion object {
        val TestClock: Clock = Clock.fixed(
            Instant.parse("2026-06-28T12:00:00Z"),
            ZoneOffset.UTC,
        )
    }
}
