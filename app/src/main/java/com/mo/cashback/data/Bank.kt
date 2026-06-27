package com.mo.cashback.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banks")
data class Bank(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val isBuiltIn: Boolean = true,
    val isSelected: Boolean = false,
    val sortOrder: Int = 0,
)
