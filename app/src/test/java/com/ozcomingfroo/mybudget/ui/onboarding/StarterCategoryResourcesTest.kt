package com.ozcomingfroo.mybudget.ui.onboarding

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ozcomingfroo.mybudget.data.preferences.AppLanguageMode
import com.ozcomingfroo.mybudget.data.repository.StarterCategoryKey
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StarterCategoryResourcesTest {
    @Test
    fun resolveTitles_returnsEnglishTitlesForEnglishMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val titles = StarterCategoryResources.resolveTitles(
            context = context,
            languageMode = AppLanguageMode.EN_US,
        ).associate { it.key to it.title }

        assertEquals("Groceries", titles[StarterCategoryKey.GROCERIES])
        assertEquals("Salary", titles[StarterCategoryKey.SALARY])
    }

    @Test
    fun resolveTitles_returnsHebrewTitlesForHebrewMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val titles = StarterCategoryResources.resolveTitles(
            context = context,
            languageMode = AppLanguageMode.HE,
        ).associate { it.key to it.title }

        assertEquals("מצרכים", titles[StarterCategoryKey.GROCERIES])
        assertEquals("משכורת", titles[StarterCategoryKey.SALARY])
    }
}
