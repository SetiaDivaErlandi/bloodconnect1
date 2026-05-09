package com.example.bloodconnect.data.repository

import com.example.bloodconnect.data.remote.ApiService
import com.example.bloodconnect.data.remote.BloodDataResponse
import com.example.bloodconnect.data.remote.UserResponse

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
