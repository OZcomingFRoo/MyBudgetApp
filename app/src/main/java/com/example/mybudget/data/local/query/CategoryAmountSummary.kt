package com.example.mybudget.data.local.query

import androidx.room.ColumnInfo

data class CategoryAmountSummary(
    @ColumnInfo(name = "category_id")
    val categoryId: Long?,
    @ColumnInfo(name = "total_minor")
    val totalMinor: Long,
)
