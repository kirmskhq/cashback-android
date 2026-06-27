package com.mo.cashback.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mo.cashback.CashbackApplication
import com.mo.cashback.repo.CashbackRepository

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (CashbackRepository) -> VM,
): VM {
    val app = LocalContext.current.applicationContext as CashbackApplication
    val repo = app.repository
    return viewModel(
        key = key,
        factory = viewModelFactory {
            initializer { create(repo) }
        },
    )
}
