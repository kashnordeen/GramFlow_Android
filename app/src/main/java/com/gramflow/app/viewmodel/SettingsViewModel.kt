package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.repository.RateSettings
import com.gramflow.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<RateSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RateSettings())

    var ratePerGramText = MutableStateFlow("")
    var special025Text = MutableStateFlow("")
    var special050Text = MutableStateFlow("")

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            val s = settingsRepo.getSettings()
            ratePerGramText.value = s.ratePerGram.toString()
            special025Text.value = s.special025.toString()
            special050Text.value = s.special050.toString()
        }
    }

    fun saveSettings(onResult: (Boolean, String?) -> Unit) {
        val r = ratePerGramText.value.toDoubleOrNull() ?: return onResult(false, "Invalid standard rate")
        val s25 = special025Text.value.toDoubleOrNull() ?: return onResult(false, "Invalid 0.25g bracket")
        val s50 = special050Text.value.toDoubleOrNull() ?: return onResult(false, "Invalid 0.50g bracket")

        viewModelScope.launch {
            _isSaving.value = true
            val res = settingsRepo.updateSettings(r, s25, s50)
            _isSaving.value = false
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }
}
