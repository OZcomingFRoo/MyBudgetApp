package com.example.mybudget.data.preferences

enum class AppThemeMode {
    DEFAULT,
    NIGHT,
    SYSTEM,
    ;

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode =
            entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
