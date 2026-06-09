package com.example.bloodconnect.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bloodconnect.data.local.UserPreferences
import com.example.bloodconnect.data.api.RetrofitClient
import com.example.bloodconnect.data.repository.BloodRepository
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel

import com.example.bloodconnect.data.repository.AuthRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val apiService by lazy { RetrofitClient.instance }

    private val bloodRepository by lazy { BloodRepository(apiService, userPreferences) }
    private val authRepository by lazy { AuthRepository(apiService) }
    private val userPreferences by lazy { UserPreferences(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, userPreferences) as T
            }
            modelClass.isAssignableFrom(BloodViewModel::class.java) -> {
                BloodViewModel(bloodRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
