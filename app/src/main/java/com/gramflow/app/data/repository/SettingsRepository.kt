package com.gramflow.app.data.repository

import com.gramflow.app.data.local.dao.SettingsDao
import com.gramflow.app.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RateSettings(
    val ratePerGram: Double = 1000.0,
    val special025: Double = 250.0,
    val special050: Double = 500.0
)

class SettingsRepository(private val settingsDao: SettingsDao) {

    val settingsFlow: Flow<RateSettings> = settingsDao.getAllSettingsFlow().map { list ->
        val map = list.associate { it.key to it.value }
        RateSettings(
            ratePerGram = map["rate_per_gram"]?.toDoubleOrNull() ?: 1000.0,
            special025 = map["special_025_030"]?.toDoubleOrNull() ?: 250.0,
            special050 = map["special_050_060"]?.toDoubleOrNull() ?: 500.0
        )
    }

    suspend fun getSettings(): RateSettings {
        val r = settingsDao.getSettingValue("rate_per_gram")?.toDoubleOrNull() ?: 1000.0
        val sp25 = settingsDao.getSettingValue("special_025_030")?.toDoubleOrNull() ?: 250.0
        val sp50 = settingsDao.getSettingValue("special_050_060")?.toDoubleOrNull() ?: 500.0
        return RateSettings(r, sp25, sp50)
    }

    suspend fun updateSettings(ratePerGram: Double, special025: Double, special050: Double): Result<Unit> {
        settingsDao.setSettings(
            listOf(
                SettingEntity("rate_per_gram", ratePerGram.toString()),
                SettingEntity("special_025_030", special025.toString()),
                SettingEntity("special_050_060", special050.toString())
            )
        )
        return Result.success(Unit)
    }
}
