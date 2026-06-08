package com.example.mybudget.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mybudget.data.local.dao.BudgetBookDao
import com.example.mybudget.data.local.dao.CategoryDao
import com.example.mybudget.data.local.dao.RecurringTransactionDao
import com.example.mybudget.data.local.dao.TransactionDao
import com.example.mybudget.data.local.entity.BudgetBookEntity
import com.example.mybudget.data.local.entity.CategoryEntity
import com.example.mybudget.data.local.entity.RecurringTransactionEntity
import com.example.mybudget.data.local.entity.TransactionEntity

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
