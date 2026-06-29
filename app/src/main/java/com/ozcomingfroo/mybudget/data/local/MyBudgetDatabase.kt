package com.ozcomingfroo.mybudget.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozcomingfroo.mybudget.data.local.dao.BudgetBookDao
import com.ozcomingfroo.mybudget.data.local.dao.CategoryDao
import com.ozcomingfroo.mybudget.data.local.dao.RecurringTransactionDao
import com.ozcomingfroo.mybudget.data.local.dao.TransactionDao
import com.ozcomingfroo.mybudget.data.local.entity.BudgetBookEntity
import com.ozcomingfroo.mybudget.data.local.entity.CategoryEntity
import com.ozcomingfroo.mybudget.data.local.entity.RecurringTransactionEntity
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity

@Database(
    entities = [
        BudgetBookEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringTransactionEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(MyBudgetTypeConverters::class)
abstract class MyBudgetDatabase : RoomDatabase() {
    abstract fun budgetBookDao(): BudgetBookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    UPDATE transactions
                    SET occurred_date = occurred_date || 'T00:00'
                    WHERE occurred_date IS NOT NULL
                    AND instr(occurred_date, 'T') = 0
                    """.trimIndent(),
                )
            }
        }

        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE budget_books
                    ADD COLUMN total_income_minor INTEGER NOT NULL DEFAULT 0
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE budget_books
                    ADD COLUMN total_expense_minor INTEGER NOT NULL DEFAULT 0
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE budget_books
                    SET total_income_minor = COALESCE((
                        SELECT SUM(amount_minor)
                        FROM transactions
                        WHERE transactions.budget_book_id = budget_books.id
                        AND transactions.type = 'INCOME'
                    ), 0)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE budget_books
                    SET total_expense_minor = COALESCE((
                        SELECT SUM(amount_minor)
                        FROM transactions
                        WHERE transactions.budget_book_id = budget_books.id
                        AND transactions.type = 'EXPENSE'
                    ), 0)
                    """.trimIndent(),
                )
            }
        }

        val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_transactions_budget_book_id_occurred_date_id
                    ON transactions(budget_book_id, occurred_date, id)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_recurring_transactions_is_active_next_run_date_id
                    ON recurring_transactions(is_active, next_run_date, id)
                    """.trimIndent(),
                )
            }
        }

        val Migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE recurring_transactions
                    ADD COLUMN schedule_weekday INTEGER
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    ALTER TABLE recurring_transactions
                    ADD COLUMN schedule_month_day INTEGER
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE recurring_transactions
                    SET schedule_weekday = CAST(strftime('%w', start_date) AS INTEGER) + 1
                    WHERE frequency = 'WEEKLY'
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE recurring_transactions
                    SET schedule_weekday = 7
                    WHERE frequency = 'WEEKLY'
                    AND schedule_weekday = 1
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE recurring_transactions
                    SET schedule_weekday = schedule_weekday - 1
                    WHERE frequency = 'WEEKLY'
                    AND schedule_weekday BETWEEN 2 AND 7
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    UPDATE recurring_transactions
                    SET schedule_month_day = MIN(CAST(strftime('%d', start_date) AS INTEGER), 30)
                    WHERE frequency = 'MONTHLY'
                    """.trimIndent(),
                )
            }
        }
    }
}
