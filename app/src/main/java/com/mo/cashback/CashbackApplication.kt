package com.mo.cashback

import android.app.Application
import com.mo.cashback.data.AppDatabase
import com.mo.cashback.repo.CashbackRepository
import com.mo.cashback.util.Reminders

class CashbackApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: CashbackRepository by lazy { CashbackRepository(database) }

    override fun onCreate() {
        super.onCreate()
        Reminders.ensureChannel(this)
        Reminders.scheduleNext(this)
    }
}
