package com.mo.cashback.ui.categories

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

class CategoryDetailViewModel(
    private val repo: CashbackRepository,
    val categoryId: String,
) : ViewModel() {
    private val now = currentYearMonth()
    val year = now.first
    val month = now.second

    val category: StateFlow<Category?> = repo.observeCategory(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val entries: StateFlow<List<EntryWithRefs>> =
        repo.observeEntriesForCategory(categoryId, year, month)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val banks: StateFlow<List<Bank>> = repo.observeSelectedBanks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addEntry(bankId: String, percent: Double, onError: (Throwable) -> Unit, onOk: () -> Unit) {
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

    fun delete(onDone: () -> Unit) {
        val c = category.value ?: return
        viewModelScope.launch {
            repo.deleteCategory(c)
            onDone()
        }
    }
}
