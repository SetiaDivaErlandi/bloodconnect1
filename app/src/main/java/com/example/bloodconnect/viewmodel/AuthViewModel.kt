package com.example.bloodconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.model.UserModel
import com.example.bloodconnect.model.local.UserPreferences
import com.example.bloodconnect.model.UserResponse
import com.example.bloodconnect.model.AuthRepository
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
    data class EmailFound(val key: String, val user: UserResponse) : AuthState()
    object PasswordResetSuccess : AuthState()
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

    val isDarkMode: StateFlow<Boolean> = userPreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        checkSessionExpiry()
    }

    private fun checkSessionExpiry() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userPreferences.isLoggedIn,
                userPreferences.lastActivityTime
            ) { loggedIn, lastActive ->
                Pair(loggedIn, lastActive)
            }.collect { (loggedIn, lastActive) ->
                if (loggedIn && lastActive > 0L && System.currentTimeMillis() - lastActive > 2592000000L) {
                    logout()
                }
            }
        }
    }

    fun updateActivity() {
        viewModelScope.launch {
            userPreferences.updateLastActivityTime()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }

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
                _authState.value = AuthState.Error(
                    if (e is java.io.IOException || e is java.net.UnknownHostException) {
                        "Koneksi internet terputus. Silakan periksa koneksi Anda."
                    } else {
                        e.message ?: "Login gagal: Terjadi kesalahan jaringan."
                    }
                )
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, bloodType: String, location: String, gender: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val newUser = authRepository.register(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    bloodType = bloodType,
                    location = location,
                    gender = gender
                )
                
                // Simpan session user baru agar dashboard tidak kosong
                userPreferences.saveLoginSession(newUser)

                _authState.value = AuthState.Success(newUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    if (e is java.io.IOException || e is java.net.UnknownHostException) {
                        "Koneksi internet terputus. Silakan periksa koneksi Anda."
                    } else {
                        e.message ?: "Registrasi gagal: Terjadi kesalahan."
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    fun updateProfile(name: String, phone: String, location: String, imageUrl: String, bloodType: String, gender: String) {
        val currentUser = userData.value ?: return
        viewModelScope.launch {
            try {
                val updatedUser = authRepository.updateProfile(
                    id = currentUser.id,
                    name = name,
                    phone = phone,
                    location = location,
                    imageUrl = imageUrl,
                    bloodType = bloodType,
                    gender = gender
                )
                if (updatedUser != null) {
                    userPreferences.saveLoginSession(updatedUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.getUserByEmailWithKey(email)
                if (result != null) {
                    _authState.value = AuthState.EmailFound(result.first, result.second)
                } else {
                    _authState.value = AuthState.Error("Email tidak terdaftar")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun resetPassword(key: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.updatePasswordOnly(key, newPassword)
                _authState.value = AuthState.PasswordResetSuccess
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Gagal mereset password: ${e.message}")
            }
        }
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = userData.value ?: return
        viewModelScope.launch {
            try {
                authRepository.updatePasswordOnly(currentUser.id, newPassword)
                val updatedUser = currentUser.copy(password = newPassword.trim())
                userPreferences.saveLoginSession(updatedUser)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan saat mengubah password")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
