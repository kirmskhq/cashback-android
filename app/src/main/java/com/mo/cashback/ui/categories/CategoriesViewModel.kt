package com.mo.cashback.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.CategoryTopOffer
import com.mo.cashback.repo.CashbackRepository
import com.mo.cashback.ui.component.currentYearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CategoriesViewModel(repo: CashbackRepository) : ViewModel() {
    private val now = currentYearMonth()
    val year = now.first
    val month = now.second
    val items: StateFlow<List<CategoryTopOffer>> =
        repo.observeCategoriesWithTop(year, month)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
