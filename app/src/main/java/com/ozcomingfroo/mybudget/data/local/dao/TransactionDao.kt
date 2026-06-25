package com.ozcomingfroo.mybudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ozcomingfroo.mybudget.data.local.entity.TransactionEntity
import com.ozcomingfroo.mybudget.data.local.model.TransactionType
import com.ozcomingfroo.mybudget.data.local.query.CategoryAmountSummary
import com.ozcomingfroo.mybudget.data.local.query.MonthlyAmountSummary
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE budget_book_id = :budgetBookId
        ORDER BY occurred_date DESC, id DESC
        """,
    )
    fun observeForBudgetBook(budgetBookId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE budget_book_id = :budgetBookId
        AND occurred_date >= :startDateTime
        AND occurred_date < :endExclusiveDateTime
        ORDER BY occurred_date DESC, id DESC
        """,
    )
    fun observeForDateRange(
        budgetBookId: Long,
        startDateTime: LocalDateTime,
        endExclusiveDateTime: LocalDateTime,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE budget_book_id = :budgetBookId
        AND occurred_date >= :startDateTime
        AND occurred_date < :endExclusiveDateTime
        AND (:type IS NULL OR type = :type)
        AND (:filterByCategory = 0 OR category_id IN (:categoryIds))
        ORDER BY occurred_date DESC, id DESC
        """,
    )
    fun observeForHistoryFilter(
        budgetBookId: Long,
        startDateTime: LocalDateTime,
        endExclusiveDateTime: LocalDateTime,
        type: TransactionType?,
        filterByCategory: Boolean,
        categoryIds: List<Long>,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM transactions
            WHERE budget_book_id = :budgetBookId
            LIMIT 1
        )
        """,
    )
    fun observeHasTransactions(budgetBookId: Long): Flow<Boolean>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions
        WHERE budget_book_id = :budgetBookId
        ORDER BY occurred_date, id
        """,
    )
    suspend fun getForBudgetBook(budgetBookId: Long): List<TransactionEntity>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query(
        """
        SELECT COALESCE(SUM(amount_minor), 0) FROM transactions
        WHERE budget_book_id = :budgetBookId
        AND type = :type
        AND occurred_date >= :startDateTime
        AND occurred_date < :endExclusiveDateTime
        """,
    )
    suspend fun totalByType(
        budgetBookId: Long,
        type: TransactionType,
        startDateTime: LocalDateTime,
        endExclusiveDateTime: LocalDateTime,
    ): Long

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN type = 'INCOME' THEN amount_minor
                WHEN type = 'EXPENSE' THEN -amount_minor
                ELSE 0
            END
        ), 0)
        FROM transactions
        WHERE budget_book_id = :budgetBookId
        """,
    )
    suspend fun currentBalance(budgetBookId: Long): Long

    @Query(
        """
        SELECT category_id, COALESCE(SUM(amount_minor), 0) AS total_minor
        FROM transactions
        WHERE budget_book_id = :budgetBookId
        AND type = :type
        AND occurred_date >= :startDateTime
        AND occurred_date < :endExclusiveDateTime
        GROUP BY category_id
        ORDER BY total_minor DESC
        """,
    )
    suspend fun totalsByCategory(
        budgetBookId: Long,
        type: TransactionType,
        startDateTime: LocalDateTime,
        endExclusiveDateTime: LocalDateTime,
    ): List<CategoryAmountSummary>

    @Query(
        """
        SELECT substr(occurred_date, 1, 7) AS month, COALESCE(SUM(amount_minor), 0) AS total_minor
        FROM transactions
        WHERE budget_book_id = :budgetBookId
        AND type = :type
        AND occurred_date >= :startDateTime
        AND occurred_date < :endExclusiveDateTime
        GROUP BY month
        ORDER BY month
        """,
    )
    suspend fun monthlyTotals(
        budgetBookId: Long,
        type: TransactionType,
        startDateTime: LocalDateTime,
        endExclusiveDateTime: LocalDateTime,
    ): List<MonthlyAmountSummary>
}
