package com.ozcomingfroo.mybudget.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = BudgetBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["budget_book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["budget_book_id"]),
        Index(value = ["category_id"]),
        Index(value = ["budget_book_id", "is_active", "next_run_date"]),
    ],
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "budget_book_id")
    val budgetBookId: Long,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    val type: TransactionType,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    val title: String? = null,
    val note: String? = null,
    val frequency: RecurringFrequency,
    val interval: Int,
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,
    @ColumnInfo(name = "next_run_date")
    val nextRunDate: LocalDate,
    @ColumnInfo(name = "last_run_date")
    val lastRunDate: LocalDate? = null,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
