package com.ozcomingfroo.mybudget.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
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
        ForeignKey(
            entity = RecurringTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurring_transaction_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["budget_book_id"]),
        Index(value = ["occurred_date"]),
        Index(value = ["type"]),
        Index(value = ["category_id"]),
        Index(value = ["recurring_transaction_id"]),
        Index(value = ["budget_book_id", "occurred_date", "id"]),
        Index(value = ["budget_book_id", "type", "occurred_date"]),
        Index(value = ["budget_book_id", "category_id", "occurred_date"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "budget_book_id")
    val budgetBookId: Long,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    @ColumnInfo(name = "recurring_transaction_id")
    val recurringTransactionId: Long? = null,
    val type: TransactionType,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    val title: String? = null,
    val note: String? = null,
    @ColumnInfo(name = "occurred_date")
    val occurredAt: LocalDateTime,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
