package com.ozcomingfroo.mybudget.data.local

import androidx.room.TypeConverter
import com.ozcomingfroo.mybudget.data.local.model.CategoryType
import com.ozcomingfroo.mybudget.data.local.model.RecurringFrequency
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class MyBudgetTypeConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let {
        if ('T' in it) LocalDateTime.parse(it) else LocalDate.parse(it).atStartOfDay()
    }

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
