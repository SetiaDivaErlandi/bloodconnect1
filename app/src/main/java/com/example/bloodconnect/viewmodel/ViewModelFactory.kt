package com.example.bloodconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bloodconnect.model.local.UserPreferences
import com.example.bloodconnect.model.api.RetrofitClient
import com.example.bloodconnect.model.BloodRepository
import com.example.bloodconnect.viewmodel.AuthViewModel
import com.example.bloodconnect.viewmodel.BloodViewModel

import com.example.bloodconnect.model.AuthRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val apiService by lazy { RetrofitClient.instance }
    private val githubApiService by lazy { RetrofitClient.githubInstance }

    private val bloodRepository by lazy { BloodRepository(apiService, githubApiService, userPreferences) }
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
