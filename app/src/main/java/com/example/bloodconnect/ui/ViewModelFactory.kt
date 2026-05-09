package com.example.bloodconnect.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bloodconnect.data.local.UserPreferences
import com.example.bloodconnect.data.remote.ApiService
import com.example.bloodconnect.data.repository.BloodRepository
import com.example.bloodconnect.ui.viewmodel.AuthViewModel
import com.example.bloodconnect.ui.viewmodel.BloodViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val bloodRepository by lazy { BloodRepository(apiService) }
    private val userPreferences by lazy { UserPreferences(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(bloodRepository, userPreferences) as T
            }
            modelClass.isAssignableFrom(BloodViewModel::class.java) -> {
                BloodViewModel(bloodRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
