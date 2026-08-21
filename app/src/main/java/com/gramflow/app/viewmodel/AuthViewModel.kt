package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.UserEntity
import com.gramflow.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    var emailText = MutableStateFlow("")
    var passwordText = MutableStateFlow("")
    var nameText = MutableStateFlow("")
    var registrationCodeText = MutableStateFlow("")

    val sessionUser: StateFlow<UserEntity?> = authRepo.activeUserFlow

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun isSessionValid(): Boolean = authRepo.isSessionValid()

    fun recordActivity() = authRepo.recordActivity()

    fun logout() = authRepo.logout()

    fun login(onSuccess: () -> Unit) {
        val email = emailText.value.trim()
        val pass = passwordText.value

        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val res = authRepo.login(email, pass)
            _isLoading.value = false

            if (res.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = res.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun signup(onSuccess: () -> Unit) {
        val email = emailText.value.trim()
        val name = nameText.value.trim()
        val pass = passwordText.value
        val code = registrationCodeText.value.trim()

        if (email.isBlank() || name.isBlank() || pass.isBlank() || code.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val res = authRepo.signup(email, name, pass, code)
            _isLoading.value = false

            if (res.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = res.exceptionOrNull()?.message ?: "Signup failed"
            }
        }
    }
}
