package com.example.mybudget.data.preferences

enum class AppLanguageMode {
    SYSTEM,
    EN_US,
    HE,
    ;

    companion object {
        fun fromStorageValue(value: String?): AppLanguageMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
