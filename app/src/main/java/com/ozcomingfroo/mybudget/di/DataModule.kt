package com.ozcomingfroo.mybudget.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.ozcomingfroo.mybudget.data.local.MyBudgetDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MyBudgetDatabase = Room.databaseBuilder(
        context,
        MyBudgetDatabase::class.java,
        "my_budget.db",
    ).addMigrations(
        MyBudgetDatabase.Migration1To2,
        MyBudgetDatabase.Migration2To3,
    ).build()

    @Provides
    fun provideBudgetBookDao(database: MyBudgetDatabase) = database.budgetBookDao()

    @Provides
    fun provideCategoryDao(database: MyBudgetDatabase) = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: MyBudgetDatabase) = database.transactionDao()

    @Provides
    fun provideRecurringTransactionDao(database: MyBudgetDatabase) =
        database.recurringTransactionDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("app_preferences")
    }
}
