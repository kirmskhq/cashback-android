package com.mo.cashback.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoDao {

    @Query("SELECT * FROM promos ORDER BY sortOrder, rowid")
    fun observeAll(): Flow<List<Promo>>

    @Query("SELECT * FROM promos WHERE id = :id")
    fun observeById(id: String): Flow<Promo?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Promo>)
}
