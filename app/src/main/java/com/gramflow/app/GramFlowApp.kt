package com.gramflow.app

import android.app.Application
import com.gramflow.app.data.local.AppDatabase
import com.gramflow.app.data.local.DataSeeder
import com.gramflow.app.data.repository.AuthRepository
import com.gramflow.app.data.repository.CustomerRepository
import com.gramflow.app.data.repository.InventoryRepository
import com.gramflow.app.data.repository.SettingsRepository

class GramFlowApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val authRepository by lazy { AuthRepository(database.userDao()) }
    val inventoryRepository by lazy { InventoryRepository(database) }
    val customerRepository by lazy { CustomerRepository(database) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    override fun onCreate() {
        super.onCreate()
        DataSeeder.seedIfEmpty(this, database)
    }
}
