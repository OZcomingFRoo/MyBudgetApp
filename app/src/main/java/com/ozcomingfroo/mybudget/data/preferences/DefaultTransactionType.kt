package com.ozcomingfroo.mybudget.data.preferences

enum class DefaultTransactionType {
    EXPENSE,
    INCOME,
    ;

    companion object {
        fun fromStorageValue(value: String?): DefaultTransactionType =
            entries.firstOrNull { it.name == value } ?: EXPENSE
    }
}
