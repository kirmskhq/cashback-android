package com.mo.cashback

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mo.cashback.ui.navigation.AppNav
import com.mo.cashback.ui.theme.CashbackTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestNotifPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignore result */ }

    override fun attachBaseContext(newBase: Context) {
        val ru = Locale("ru")
        Locale.setDefault(ru)
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(ru)
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeAskForNotificationPermission()
        setContent {
            CashbackTheme {
                AppNav()
            }
        }
    }

    private fun maybeAskForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val perm = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) return
        requestNotifPerm.launch(perm)
    }
}
