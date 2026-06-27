package com.mo.cashback.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mo.cashback.data.EntryWithRefs
import com.mo.cashback.repo.CashbackRepository
import com.mo.cashback.ui.component.currentYearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val repo: CashbackRepository) : ViewModel() {

    private val now = currentYearMonth()
    private val _yearMonth = MutableStateFlow(YearMonth.of(now.first, now.second))
    val yearMonth: StateFlow<YearMonth> = _yearMonth.asStateFlow()

    val entries: StateFlow<List<EntryWithRefs>> =
        _yearMonth.flatMapLatest { ym ->
            repo.observeEntriesForMonth(ym.year, ym.monthValue)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun prev() { _yearMonth.value = _yearMonth.value.minusMonths(1) }
    fun next() { _yearMonth.value = _yearMonth.value.plusMonths(1) }
    fun today() { _yearMonth.value = YearMonth.of(now.first, now.second) }
}
