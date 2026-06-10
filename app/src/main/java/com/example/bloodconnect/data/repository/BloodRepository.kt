package com.example.bloodconnect.data.repository

import com.example.bloodconnect.data.api.ApiService
import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.UserResponse
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.data.model.Article
import com.example.bloodconnect.data.model.SosRequestResponse
import com.example.bloodconnect.data.model.DonationResponse
import kotlinx.coroutines.flow.Flow

class BloodRepository(
    private val apiService: ApiService,
    private val userPreferences: com.example.bloodconnect.data.local.UserPreferences
) {
    val bookmarks: Flow<Set<String>> = userPreferences.bookmarks
    val readHistory: Flow<Set<String>> = userPreferences.readHistory

    suspend fun toggleBookmark(title: String) {
        userPreferences.toggleBookmark(title)
    }

    suspend fun addArticleToHistory(title: String) {
        userPreferences.addArticleToHistory(title)
    }

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
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            try {
                apiService.getBloodData()
            } catch (ex: Exception) {
                if (ex is java.io.IOException || ex is java.net.UnknownHostException) {
                    throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
                }
                BloodDataResponse(emptyList(), emptyList())
            }
        }
    }

    suspend fun getUsers(): List<UserResponse> {
        return try {
            val firebaseUsers = apiService.getFirebaseUsers()
            firebaseUsers?.values?.toList() ?: apiService.getUsers()
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            apiService.getUsers()
        }
    }

    suspend fun getSosRequests(): List<SosRequestResponse> {
        return try {
            val map = apiService.getFirebaseSosRequests()
            map?.values?.toList()?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            emptyList()
        }
    }

    suspend fun saveSosRequest(request: SosRequestResponse): Boolean {
        return try {
            apiService.saveFirebaseSosRequest(request.id, request)
            true
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            false
        }
    }

    suspend fun deleteSosRequest(id: String): Boolean {
        return try {
            val response = apiService.deleteFirebaseSosRequest(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun completeSosRequest(request: SosRequestResponse): Boolean {
        return try {
            // Simpan ke riwayat
            apiService.saveFirebaseSosHistory(request.requesterId, request.id, request)
            // Hapus dari daftar aktif
            apiService.deleteFirebaseSosRequest(request.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSosHistory(userId: String): List<SosRequestResponse> {
        return try {
            val map = apiService.getFirebaseSosHistory(userId)
            map?.values?.toList()?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDonations(userId: String): List<DonationResponse> {
        return try {
            val map = apiService.getFirebaseDonations(userId)
            map?.values?.toList() ?: emptyList()
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            emptyList()
        }
    }

    suspend fun saveDonation(userId: String, donation: DonationResponse): Boolean {
        return try {
            apiService.saveFirebaseDonation(userId, donation.id, donation)
            true
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            false
        }
    }
}
