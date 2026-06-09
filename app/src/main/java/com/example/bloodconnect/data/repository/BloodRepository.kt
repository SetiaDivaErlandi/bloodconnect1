package com.example.bloodconnect.data.repository

import com.example.bloodconnect.data.api.ApiService
import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.UserResponse

class BloodRepository(
    private val apiService: ApiService
) {
    suspend fun getBloodData(): BloodDataResponse {
        return apiService.getBloodData()
    }

    suspend fun getUsers(): List<UserResponse> {
        return apiService.getUsers()
    }
}
