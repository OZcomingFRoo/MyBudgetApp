package com.ozcomingfroo.mybudget.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
    exportSchema = false,
)
@TypeConverters(MyBudgetTypeConverters::class)
abstract class MyBudgetDatabase : RoomDatabase() {
    abstract fun budgetBookDao(): BudgetBookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
}
