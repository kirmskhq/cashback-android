package com.mo.cashback.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val nameResKey: String?,
    val emoji: String,
    val isBuiltIn: Boolean,
)
