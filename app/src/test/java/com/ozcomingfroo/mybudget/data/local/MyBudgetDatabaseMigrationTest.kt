package com.ozcomingfroo.mybudget.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MyBudgetDatabaseMigrationTest {
    @Test
    fun migration4To5_addsAndBackfillsRecurringScheduleAnchors() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(4) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE recurring_transactions (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    budget_book_id INTEGER NOT NULL,
                                    category_id INTEGER,
                                    type TEXT NOT NULL,
                                    amount_minor INTEGER NOT NULL,
                                    title TEXT,
                                    note TEXT,
                                    frequency TEXT NOT NULL,
                                    interval INTEGER NOT NULL,
                                    start_date TEXT NOT NULL,
                                    end_date TEXT,
                                    next_run_date TEXT NOT NULL,
                                    last_run_date TEXT,
                                    is_active INTEGER NOT NULL,
                                    created_at INTEGER NOT NULL,
                                    updated_at INTEGER NOT NULL
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                INSERT INTO recurring_transactions (
                                    budget_book_id, type, amount_minor, frequency, interval,
                                    start_date, next_run_date, is_active, created_at, updated_at
                                ) VALUES
                                (1, 'EXPENSE', 100, 'WEEKLY', 1, '2026-06-15', '2026-06-15', 1, 0, 0),
                                (1, 'EXPENSE', 100, 'MONTHLY', 1, '2026-01-31', '2026-01-31', 1, 0, 0),
                                (1, 'EXPENSE', 100, 'DAILY', 1, '2026-06-16', '2026-06-16', 1, 0, 0)
                                """.trimIndent(),
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        MyBudgetDatabase.Migration4To5.migrate(db)

        db.query(
            """
            SELECT frequency, schedule_weekday, schedule_month_day
            FROM recurring_transactions
            ORDER BY id
            """.trimIndent(),
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("WEEKLY", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
            assertNull(cursor.getString(2))

            cursor.moveToNext()
            assertEquals("MONTHLY", cursor.getString(0))
            assertNull(cursor.getString(1))
            assertEquals(30, cursor.getInt(2))

            cursor.moveToNext()
            assertEquals("DAILY", cursor.getString(0))
            assertNull(cursor.getString(1))
            assertNull(cursor.getString(2))
        }

        helper.close()
    }
}
