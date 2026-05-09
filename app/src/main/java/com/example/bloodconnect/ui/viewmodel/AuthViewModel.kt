package com.example.bloodconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.data.local.UserModel
import com.example.bloodconnect.data.local.UserPreferences
import com.example.bloodconnect.data.repository.BloodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserModel) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: BloodRepository,
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
                // 1. Check local registered user first (High priority for simulation)
                val regUser = userPreferences.registeredUser.first()
                if (regUser["email"] == trimmedEmail && regUser["password"] == trimmedPassword) {
                    val userModel = UserModel(
                        id = "local_user",
                        name = regUser["name"] ?: "User",
                        email = regUser["email"] ?: "",
                        bloodType = regUser["bloodType"] ?: "O+",
                        location = regUser["location"] ?: "Lampung",
                        imageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=${regUser["name"]}",
                        phone = regUser["phone"] ?: ""
                    )
                    userPreferences.saveLoginSession(userModel)
                    _authState.value = AuthState.Success(userModel)
                    return@launch
                }

                // 2. Fallback to Remote API
                val users = repository.getUsers()
                val user = users.find { it.email.trim() == trimmedEmail && it.password.trim() == trimmedPassword }
                
                if (user != null) {
                    val userModel = UserModel(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        bloodType = user.bloodType,
                        location = user.location,
                        imageUrl = user.imageUrl,
                        phone = user.phone
                    )
                    userPreferences.saveLoginSession(userModel)
                    _authState.value = AuthState.Success(userModel)
                } else {
                    _authState.value = AuthState.Error("Email atau Password salah.")
                }
            } catch (e: Exception) {
                // If API fails but user exists locally, we already handled it above.
                // If we reach here, it means both failed.
                _authState.value = AuthState.Error("Login gagal: Akun tidak ditemukan.")
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, bloodType: String, location: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val newUser = UserModel(
                id = "local_${System.currentTimeMillis()}",
                name = name.trim(),
                email = email.trim(),
                bloodType = bloodType,
                location = location,
                imageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$name",
                phone = phone.trim()
            )
            
            // Persist locally so login works after logout
            userPreferences.saveRegisteredUser(newUser, password.trim())
            // Set current session
            userPreferences.saveLoginSession(newUser)

            _authState.value = AuthState.Success(newUser)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
