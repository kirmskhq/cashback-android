package com.mo.cashback.repo

import com.mo.cashback.data.AppDatabase
import com.mo.cashback.data.Bank
import com.mo.cashback.data.BankWithCount
import com.mo.cashback.data.CashbackEntry
import com.mo.cashback.data.Category
import com.mo.cashback.data.CategoryTopOffer
import com.mo.cashback.data.EntryWithRefs
import com.mo.cashback.data.Promo
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CashbackRepository(private val db: AppDatabase) {

    fun observeBanks() = db.bankDao().observeAll()
    fun observeSelectedBanks() = db.bankDao().observeSelected()
    fun observeBank(id: String) = db.bankDao().observeById(id)
    fun observeBanksWithCount(year: Int, month: Int): Flow<List<BankWithCount>> =
        db.bankDao().observeSelectedWithCount(year, month)
    fun observeCustomBankCount(): Flow<Int> = db.bankDao().observeCustomCount()

    suspend fun setBankSelected(id: String, selected: Boolean) =
        db.bankDao().setSelected(id, selected)

    suspend fun addCustomBank(name: String, colorHex: String): Bank? {
        if (db.bankDao().customCount() >= 3) return null
        val id = "u_" + java.util.UUID.randomUUID().toString().take(8)
        val bank = Bank(
            id = id,
            name = name,
            colorHex = colorHex,
            isBuiltIn = false,
            isSelected = true,
            sortOrder = 200 + db.bankDao().customCount(),
        )
        db.bankDao().insert(bank)
        return bank
    }

    suspend fun deleteCustomBank(id: String) = db.bankDao().deleteCustom(id)

    fun observeCategories() = db.categoryDao().observeAll()
    fun observeCategory(id: String) = db.categoryDao().observeById(id)
    fun observeCategoriesWithTop(year: Int, month: Int): Flow<List<CategoryTopOffer>> =
        db.categoryDao().observeAllWithTop(year, month)

    fun observeEntriesForBank(bankId: String, year: Int, month: Int): Flow<List<EntryWithRefs>> =
        db.entryDao().observeForBankMonth(bankId, year, month)

    fun observeEntriesForCategory(categoryId: String, year: Int, month: Int): Flow<List<EntryWithRefs>> =
        db.entryDao().observeForCategoryMonth(categoryId, year, month)

    fun observeEntriesForMonth(year: Int, month: Int): Flow<List<EntryWithRefs>> =
        db.entryDao().observeForMonth(year, month)

    suspend fun addEntry(bankId: String, categoryId: String, year: Int, month: Int, percent: Double): Long {
        return db.entryDao().insert(
            CashbackEntry(
                bankId = bankId,
                categoryId = categoryId,
                year = year,
                month = month,
                percent = percent,
            )
        )
    }

    suspend fun deleteEntry(id: Long) = db.entryDao().deleteById(id)

    suspend fun addCustomCategory(name: String, emoji: String): Category {
        val cat = Category(
            id = "u_" + UUID.randomUUID().toString().take(8),
            name = name,
            nameResKey = null,
            emoji = emoji,
            isBuiltIn = false,
        )
        db.categoryDao().insert(cat)
        return cat
    }

    suspend fun deleteCategory(category: Category) {
        if (category.isBuiltIn) return
        db.categoryDao().delete(category)
    }

    fun observePromos(): Flow<List<Promo>> = db.promoDao().observeAll()
    fun observePromo(id: String): Flow<Promo?> = db.promoDao().observeById(id)
}
