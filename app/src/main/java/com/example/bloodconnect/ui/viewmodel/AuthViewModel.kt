package com.example.bloodconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.data.model.UserModel
import com.example.bloodconnect.data.local.UserPreferences
import com.example.bloodconnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserModel) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val isLoggedIn: StateFlow<Boolean> = userPreferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userData: StateFlow<UserModel?> = userPreferences.userData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()
            try {
                val user = authRepository.login(trimmedEmail, trimmedPassword)
                if (user != null) {
                    userPreferences.saveLoginSession(user)
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Login gagal: Akun tidak ditemukan.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login gagal: Terjadi kesalahan jaringan.")
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, bloodType: String, location: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val newUser = authRepository.register(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    bloodType = bloodType,
                    location = location
                )
                userPreferences.saveLoginSession(newUser)
                _authState.value = AuthState.Success(newUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registrasi gagal: Terjadi kesalahan.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    fun updateProfile(name: String, phone: String, location: String) {
        val currentUser = userData.value ?: return
        viewModelScope.launch {
            try {
                val updatedUser = authRepository.updateProfile(
                    id = currentUser.id,
                    name = name,
                    phone = phone,
                    location = location
                )
                if (updatedUser != null) {
                    userPreferences.saveLoginSession(updatedUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
