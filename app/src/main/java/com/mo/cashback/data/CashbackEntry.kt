package com.mo.cashback.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    indices = [
        Index(value = ["bankId", "categoryId", "year", "month"], unique = true),
        Index(value = ["bankId"]),
        Index(value = ["categoryId"]),
        Index(value = ["year", "month"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Bank::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CashbackEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankId: String,
    val categoryId: String,
    val year: Int,
    val month: Int,
    val percent: Double,
)
