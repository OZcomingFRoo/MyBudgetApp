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
    version = 3,
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
    }
}
