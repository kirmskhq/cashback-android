package com.mo.cashback.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY isBuiltIn DESC, rowid")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: String): Flow<Category?>

    /**
     * Top bank per category (for the given month) considering ONLY selected banks,
     * plus a count of selected banks offering this category in that month.
     */
    @Query("""
        SELECT c.*,
               topBank.id        AS b_id,
               topBank.name      AS b_name,
               topBank.colorHex  AS b_colorHex,
               topBank.isBuiltIn AS b_isBuiltIn,
               topBank.isSelected AS b_isSelected,
               topBank.sortOrder AS b_sortOrder,
               topEntry.percent  AS topPercent,
               (SELECT COUNT(*) FROM entries e2
                JOIN banks b2 ON b2.id = e2.bankId
                WHERE e2.categoryId = c.id
                  AND e2.year = :year AND e2.month = :month
                  AND b2.isSelected = 1) AS bankCount
        FROM categories c
        LEFT JOIN (
            SELECT e.categoryId, MAX(e.percent) AS maxPct
            FROM entries e
            JOIN banks b ON b.id = e.bankId
            WHERE e.year = :year AND e.month = :month AND b.isSelected = 1
            GROUP BY e.categoryId
        ) maxE ON maxE.categoryId = c.id
        LEFT JOIN entries topEntry
            ON topEntry.categoryId = c.id
           AND topEntry.year = :year
           AND topEntry.month = :month
           AND topEntry.percent = maxE.maxPct
        LEFT JOIN banks topBank
            ON topBank.id = topEntry.bankId
           AND topBank.isSelected = 1
        GROUP BY c.id
        ORDER BY
            CASE WHEN bankCount > 0 THEN 0 ELSE 1 END,
            topPercent DESC,
            c.isBuiltIn DESC,
            c.rowid
    """)
    fun observeAllWithTop(year: Int, month: Int): Flow<List<CategoryTopOffer>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
