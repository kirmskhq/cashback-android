package com.mo.cashback.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {

    @Query("SELECT * FROM banks ORDER BY sortOrder, rowid")
    fun observeAll(): Flow<List<Bank>>

    @Query("SELECT * FROM banks WHERE isSelected = 1 ORDER BY sortOrder, rowid")
    fun observeSelected(): Flow<List<Bank>>

    @Query("SELECT * FROM banks WHERE id = :id")
    fun observeById(id: String): Flow<Bank?>

    @Query("""
        SELECT b.*, (
            SELECT COUNT(*) FROM entries e
            WHERE e.bankId = b.id AND e.year = :year AND e.month = :month
        ) AS activeCount
        FROM banks b
        WHERE b.isSelected = 1
        ORDER BY b.sortOrder, b.rowid
    """)
    fun observeSelectedWithCount(year: Int, month: Int): Flow<List<BankWithCount>>

    @Query("SELECT COUNT(*) FROM banks WHERE isBuiltIn = 0")
    fun observeCustomCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM banks WHERE isBuiltIn = 0")
    suspend fun customCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(banks: List<Bank>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bank: Bank)

    @Update
    suspend fun update(bank: Bank)

    @Query("UPDATE banks SET isSelected = :selected WHERE id = :id")
    suspend fun setSelected(id: String, selected: Boolean)

    @Query("DELETE FROM banks WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustom(id: String)
}
