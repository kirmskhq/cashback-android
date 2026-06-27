package com.mo.cashback.data

import androidx.room.Embedded

data class EntryWithRefs(
    @Embedded val entry: CashbackEntry,
    @Embedded(prefix = "b_") val bank: Bank,
    @Embedded(prefix = "c_") val category: Category,
)

data class BankWithCount(
    @Embedded val bank: Bank,
    val activeCount: Int,
)

data class CategoryTopOffer(
    @Embedded val category: Category,
    @Embedded(prefix = "b_") val topBank: Bank?,
    val topPercent: Double?,
    val bankCount: Int,
)
