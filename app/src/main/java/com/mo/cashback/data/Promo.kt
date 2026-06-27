package com.mo.cashback.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promos")
data class Promo(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val benefits: String,
    val url: String,
    val accentHex: String,
    val sortOrder: Int = 0,
)
