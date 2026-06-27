package com.mo.cashback.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mo.cashback.R
import com.mo.cashback.ui.banks.BankDetailScreen
import com.mo.cashback.ui.banks.BankPickerScreen
import com.mo.cashback.ui.banks.BanksScreen
import com.mo.cashback.ui.categories.CategoriesScreen
import com.mo.cashback.ui.categories.CategoryDetailScreen
import com.mo.cashback.ui.history.HistoryScreen
import com.mo.cashback.ui.promos.PromoDetailScreen
import com.mo.cashback.ui.promos.PromosScreen

private object Routes {
    const val BANKS = "banks"
    const val BANK_DETAIL = "banks/{bankId}"
    const val BANK_PICKER = "banks/picker"
    const val CATEGORIES = "categories"
    const val CATEGORY_DETAIL = "categories/{categoryId}"
    const val HISTORY = "history"
    const val PROMOS = "promos"
    const val PROMO_DETAIL = "promos/{promoId}"
    fun bankDetail(id: String) = "banks/$id"
    fun categoryDetail(id: String) = "categories/$id"
    fun promoDetail(id: String) = "promos/$id"
}

private data class TabItem(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val tabs = listOf(
        TabItem(Routes.BANKS, R.string.nav_banks, Icons.Default.AccountBalance),
        TabItem(Routes.CATEGORIES, R.string.nav_categories, Icons.Default.Category),
        TabItem(Routes.HISTORY, R.string.nav_history, Icons.Default.History),
        TabItem(Routes.PROMOS, R.string.nav_promos, Icons.Default.LocalOffer),
    )
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in setOf(Routes.BANKS, Routes.CATEGORIES, Routes.HISTORY, Routes.PROMOS)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Routes.BANKS,
            modifier = Modifier.padding(pad),
        ) {
            composable(Routes.BANKS) {
                BanksScreen(
                    onBankClick = { id -> nav.navigate(Routes.bankDetail(id)) },
                    onOpenPicker = { nav.navigate(Routes.BANK_PICKER) },
                )
            }
            composable(Routes.BANK_PICKER) {
                BankPickerScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.BANK_DETAIL) { backEntry ->
                val id = backEntry.arguments?.getString("bankId") ?: return@composable
                BankDetailScreen(bankId = id, onBack = { nav.popBackStack() })
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(onCategoryClick = { id -> nav.navigate(Routes.categoryDetail(id)) })
            }
            composable(Routes.CATEGORY_DETAIL) { backEntry ->
                val id = backEntry.arguments?.getString("categoryId") ?: return@composable
                CategoryDetailScreen(categoryId = id, onBack = { nav.popBackStack() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen()
            }
            composable(Routes.PROMOS) {
                PromosScreen(onPromoClick = { id -> nav.navigate(Routes.promoDetail(id)) })
            }
            composable(Routes.PROMO_DETAIL) { backEntry ->
                val id = backEntry.arguments?.getString("promoId") ?: return@composable
                PromoDetailScreen(promoId = id, onBack = { nav.popBackStack() })
            }
        }
    }
}
