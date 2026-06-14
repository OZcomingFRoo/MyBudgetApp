package com.ozcomingfroo.mybudget.data

import com.ozcomingfroo.mybudget.data.repository.BudgetBookRepository
import com.ozcomingfroo.mybudget.domain.recurring.RecurringTransactionGenerator
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
