package com.mo.cashback.ui.banks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.Bank
import com.mo.cashback.repo.CashbackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankPickerViewModel(private val repo: CashbackRepository) : ViewModel() {

    val allBanks: StateFlow<List<Bank>> = repo.observeBanks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customCount: StateFlow<Int> = repo.observeCustomBankCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun toggleSelected(bank: Bank) {
        viewModelScope.launch { repo.setBankSelected(bank.id, !bank.isSelected) }
    }

    fun addCustom(name: String, colorHex: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repo.addCustomBank(name.trim(), colorHex)
            onResult(result != null)
        }
    }

    fun deleteCustom(bank: Bank) {
        if (bank.isBuiltIn) return
        viewModelScope.launch { repo.deleteCustomBank(bank.id) }
    }
}
