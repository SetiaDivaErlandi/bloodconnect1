package com.example.bloodconnect.data.repository

import com.example.bloodconnect.data.api.ApiService
import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.UserResponse
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.data.model.Article

class BloodRepository(
    private val apiService: ApiService
) {
    suspend fun getBloodData(): BloodDataResponse {
        return try {
            val firebaseDonors = apiService.getFirebaseDonors()
            val firebaseArticles = apiService.getFirebaseArticles()

            if (firebaseDonors != null && firebaseArticles != null) {
                BloodDataResponse(
                    donors = firebaseDonors.values.toList(),
                    articles = firebaseArticles.values.toList()
                )
            } else {
                // Seeding Phase: Fetch from GitHub and write to Firebase Realtime Database
                val gitHubData = apiService.getBloodData()
                
                gitHubData.donors.forEach { donor ->
                    apiService.saveFirebaseDonor(donor.id, donor)
                }
                
                gitHubData.articles.forEach { article ->
                    apiService.saveFirebaseArticle(article.id.toString(), article)
                }
                
                gitHubData
            }
        } catch (e: Exception) {
            // Fallback: If Firebase setup fails or rules are closed, read from GitHub Raw
            try {
                apiService.getBloodData()
            } catch (ex: Exception) {
                // Secondary Fallback: Empty response
                BloodDataResponse(emptyList(), emptyList())
            }
        }
    }

    suspend fun getUsers(): List<UserResponse> {
        return try {
            val firebaseUsers = apiService.getFirebaseUsers()
            firebaseUsers?.values?.toList() ?: apiService.getUsers()
        } catch (e: Exception) {
            apiService.getUsers()
        }
    }
}
