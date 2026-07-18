package com.ozcomingfroo.mybudget.ui.components

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ozcomingfroo.mybudget.R
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMyBudgetDatePickerState(
    initialSelectedDateMillis: Long? = null,
): DatePickerState {
    val locale = myBudgetDatePickerLocale()
    return remember(locale, initialSelectedDateMillis) {
        DatePickerState(
            locale = locale,
            initialSelectedDateMillis = initialSelectedDateMillis,
            initialDisplayedMonthMillis = initialSelectedDateMillis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyBudgetDatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = myBudgetDatePickerLocale()
    val title = remember(context, locale) {
        context.getStringForLocale(R.string.date_picker_title, locale)
    }
    val noDateSelected = remember(context, locale) {
        context.getStringForLocale(R.string.date_picker_no_date_selected, locale)
    }
    val selectedDateText = remember(state.selectedDateMillis, locale) {
        state.selectedDateMillis?.let { selectedDateMillis ->
            Instant.ofEpochMilli(selectedDateMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
        }
    } ?: noDateSelected

    DatePicker(
        state = state,
        modifier = modifier,
        title = {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 24.dp, end = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        headline = {
            Text(
                text = selectedDateText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
}

@Composable
private fun myBudgetDatePickerLocale(): Locale {
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val configurationLocale = configuration.locales[0]
    return if (layoutDirection == LayoutDirection.Rtl && !configurationLocale.isHebrew()) {
        HebrewLocale
    } else {
        configurationLocale
    }
}

private fun Context.getStringForLocale(
    @StringRes resId: Int,
    locale: Locale,
): String {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration).getString(resId)
}

private fun Locale.isHebrew(): Boolean = language == "he" || language == "iw"

@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")
