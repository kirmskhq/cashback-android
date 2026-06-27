package com.mo.cashback.ui.banks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.Bank
import com.mo.cashback.data.Category
import com.mo.cashback.data.EntryWithRefs
import com.mo.cashback.repo.CashbackRepository
import com.mo.cashback.ui.component.currentYearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankDetailViewModel(
    private val repo: CashbackRepository,
    val bankId: String,
) : ViewModel() {
    private val now = currentYearMonth()
    val year = now.first
    val month = now.second
    val prevYear: Int = if (month == 1) year - 1 else year
    val prevMonth: Int = if (month == 1) 12 else month - 1

    val bank: StateFlow<Bank?> = repo.observeBank(bankId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val entries: StateFlow<List<EntryWithRefs>> =
        repo.observeEntriesForBank(bankId, year, month)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val previousMonthEntries: StateFlow<List<EntryWithRefs>> =
        repo.observeEntriesForBank(bankId, prevYear, prevMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> =
        repo.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun copyFromPreviousMonth() {
        viewModelScope.launch {
            previousMonthEntries.value.forEach { row ->
                try {
                    repo.addEntry(bankId, row.entry.categoryId, year, month, row.entry.percent)
                } catch (_: Throwable) { /* skip duplicates */ }
            }
        }
    }

    fun addEntry(categoryId: String, percent: Double, onError: (Throwable) -> Unit, onOk: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.addEntry(bankId, categoryId, year, month, percent)
                onOk()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    fun deleteEntry(id: Long) = viewModelScope.launch { repo.deleteEntry(id) }

    fun addCustomCategory(name: String, emoji: String) = viewModelScope.launch {
        repo.addCustomCategory(name, emoji)
    }
}
