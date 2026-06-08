package com.example.mybudget.data.local

import androidx.room.TypeConverter
import com.example.mybudget.data.local.model.CategoryType
import com.example.mybudget.data.local.model.RecurringFrequency
import com.example.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate

class MyBudgetTypeConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): TransactionType? =
        value?.let(TransactionType::valueOf)

    @TypeConverter
    fun categoryTypeToString(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun stringToCategoryType(value: String?): CategoryType? =
        value?.let(CategoryType::valueOf)

    @TypeConverter
    fun recurringFrequencyToString(value: RecurringFrequency?): String? = value?.name

    @TypeConverter
    fun stringToRecurringFrequency(value: String?): RecurringFrequency? =
        value?.let(RecurringFrequency::valueOf)
}
