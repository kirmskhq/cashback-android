package com.mo.cashback.ui.promos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.Promo
import com.mo.cashback.repo.CashbackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PromosViewModel(repo: CashbackRepository) : ViewModel() {
    val items: StateFlow<List<Promo>> = repo.observePromos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class PromoDetailViewModel(repo: CashbackRepository, val promoId: String) : ViewModel() {
    val promo: StateFlow<Promo?> = repo.observePromo(promoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
