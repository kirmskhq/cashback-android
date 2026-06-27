package com.mo.cashback.ui.banks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.BankWithCount
import com.mo.cashback.repo.CashbackRepository
import com.mo.cashback.ui.component.currentYearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BanksViewModel(private val repo: CashbackRepository) : ViewModel() {
    private val now = currentYearMonth()
    val year = now.first
    val month = now.second
    val banks: StateFlow<List<BankWithCount>> =
        repo.observeBanksWithCount(year, month)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
