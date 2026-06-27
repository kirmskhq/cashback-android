package com.mo.cashback.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CashbackEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: CashbackEntry): Long

    @Delete
    suspend fun delete(entry: CashbackEntry)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT e.*,
               b.id AS b_id, b.name AS b_name, b.colorHex AS b_colorHex,
               b.isBuiltIn AS b_isBuiltIn, b.isSelected AS b_isSelected, b.sortOrder AS b_sortOrder,
               c.id AS c_id, c.name AS c_name, c.nameResKey AS c_nameResKey,
               c.emoji AS c_emoji, c.isBuiltIn AS c_isBuiltIn
        FROM entries e
        JOIN banks b ON b.id = e.bankId
        JOIN categories c ON c.id = e.categoryId
        WHERE e.bankId = :bankId AND e.year = :year AND e.month = :month
        ORDER BY e.percent DESC, c.rowid
    """)
    fun observeForBankMonth(bankId: String, year: Int, month: Int): Flow<List<EntryWithRefs>>

    /**
     * Entries for a given category in a given month, FILTERED to selected banks only
     * (so the ranked list on the Category detail screen reflects only banks the user
     * currently has). Historic entries on un-selected banks remain in the DB and
     * still appear in the History tab.
     */
    @Query("""
        SELECT e.*,
               b.id AS b_id, b.name AS b_name, b.colorHex AS b_colorHex,
               b.isBuiltIn AS b_isBuiltIn, b.isSelected AS b_isSelected, b.sortOrder AS b_sortOrder,
               c.id AS c_id, c.name AS c_name, c.nameResKey AS c_nameResKey,
               c.emoji AS c_emoji, c.isBuiltIn AS c_isBuiltIn
        FROM entries e
        JOIN banks b ON b.id = e.bankId
        JOIN categories c ON c.id = e.categoryId
        WHERE e.categoryId = :categoryId AND e.year = :year AND e.month = :month
          AND b.isSelected = 1
        ORDER BY e.percent DESC, b.sortOrder, b.rowid
    """)
    fun observeForCategoryMonth(categoryId: String, year: Int, month: Int): Flow<List<EntryWithRefs>>

    /**
     * All entries for a given month — NOT filtered by isSelected. History shows
     * every entry that ever existed, including those on banks the user later
     * un-selected.
     */
    @Query("""
        SELECT e.*,
               b.id AS b_id, b.name AS b_name, b.colorHex AS b_colorHex,
               b.isBuiltIn AS b_isBuiltIn, b.isSelected AS b_isSelected, b.sortOrder AS b_sortOrder,
               c.id AS c_id, c.name AS c_name, c.nameResKey AS c_nameResKey,
               c.emoji AS c_emoji, c.isBuiltIn AS c_isBuiltIn
        FROM entries e
        JOIN banks b ON b.id = e.bankId
        JOIN categories c ON c.id = e.categoryId
        WHERE e.year = :year AND e.month = :month
        ORDER BY b.sortOrder, b.rowid, e.percent DESC
    """)
    fun observeForMonth(year: Int, month: Int): Flow<List<EntryWithRefs>>
}
