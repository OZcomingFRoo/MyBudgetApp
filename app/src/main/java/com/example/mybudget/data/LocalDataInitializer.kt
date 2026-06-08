package com.example.mybudget.data

import com.example.mybudget.data.repository.BudgetBookRepository
import com.example.mybudget.domain.recurring.RecurringTransactionGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataInitializer @Inject constructor(
    private val budgetBookRepository: BudgetBookRepository,
    private val recurringTransactionGenerator: RecurringTransactionGenerator,
) {
    suspend fun initialize() {
        budgetBookRepository.ensureDefaultBudgetBook()
        recurringTransactionGenerator.generateDue()
    }
}
