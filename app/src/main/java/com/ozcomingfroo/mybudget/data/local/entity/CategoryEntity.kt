package com.ozcomingfroo.mybudget.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import java.time.Instant

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = BudgetBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["budget_book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["budget_book_id"]),
        Index(value = ["budget_book_id", "type"]),
        Index(value = ["budget_book_id", "archived_at"]),
    ],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "budget_book_id")
    val budgetBookId: Long,
    val title: String,
    val type: CategoryType,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    val color: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "archived_at")
    val archivedAt: Instant? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
