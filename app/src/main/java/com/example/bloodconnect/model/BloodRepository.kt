package com.example.bloodconnect.model

import com.example.bloodconnect.model.api.ApiService
import com.example.bloodconnect.model.BloodDataResponse
import com.example.bloodconnect.model.UserResponse
import com.example.bloodconnect.model.Donor
import com.example.bloodconnect.model.Article
import com.example.bloodconnect.model.SosRequestResponse
import com.example.bloodconnect.model.DonationResponse
import com.example.bloodconnect.model.ChatMessage
import com.example.bloodconnect.model.ChatListEntry
import com.example.bloodconnect.model.local.UserPreferences
import kotlinx.coroutines.flow.Flow

class BloodRepository(
    private val apiService: ApiService,
    private val githubApiService: ApiService,
    private val userPreferences: UserPreferences
) {
    val bookmarks: Flow<Set<String>> = userPreferences.bookmarks
    val readHistory: Flow<Set<String>> = userPreferences.readHistory
    val readSosIds: Flow<Set<String>> = userPreferences.readSosIds

    suspend fun markSosAsRead(sosId: String) {
        userPreferences.markSosAsRead(sosId)
    }

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
                val sanitizedDonors = firebaseDonors.values.toList().map { donor ->
                    if (donor.phone.isNullOrBlank() || donor.phone == "null") {
                        donor.copy(phone = "08123456789")
                    } else {
                        donor
                    }
                }
                BloodDataResponse(
                    donors = sanitizedDonors,
                    articles = firebaseArticles.values.toList()
                )
            } else {
                val gitHubData = githubApiService.getBloodData()
                val sanitizedDonors = gitHubData.donors.map { donor ->
                    val cleanPhone = if (donor.phone.isNullOrBlank() || donor.phone == "null") "08123456789" else donor.phone
                    donor.copy(phone = cleanPhone)
                }
                sanitizedDonors.forEach { donor ->
                    apiService.saveFirebaseDonor(donor.id, donor)
                }
                gitHubData.articles.forEach { article ->
                    apiService.saveFirebaseArticle(article.id.toString(), article)
                }
                BloodDataResponse(sanitizedDonors, gitHubData.articles)
            }
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            try {
                val gitHubData = githubApiService.getBloodData()
                val sanitizedDonors = gitHubData.donors.map { donor ->
                    val cleanPhone = if (donor.phone.isNullOrBlank() || donor.phone == "null") "08123456789" else donor.phone
                    donor.copy(phone = cleanPhone)
                }
                BloodDataResponse(sanitizedDonors, gitHubData.articles)
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
            firebaseUsers?.values?.toList() ?: githubApiService.getUsers()
        } catch (e: Exception) {
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                throw Exception("Koneksi internet terputus. Silakan periksa koneksi Anda.")
            }
            githubApiService.getUsers()
        }
    }

    suspend fun getSosRequests(): List<SosRequestResponse> {
        return try {
            val map = apiService.getFirebaseSosRequests()
            if (map.isNullOrEmpty()) {
                val mockSosList = listOf(
                    SosRequestResponse(
                        id = "sos_mock_1",
                        requesterName = "Roni Wijaya",
                        bloodType = "B+",
                        location = "RSUD Abdul Moeloek",
                        quantity = 3,
                        notes = "Butuh donor darah B+ mendesak untuk tindakan operasi kecelakaan. Terima kasih.",
                        timestamp = System.currentTimeMillis() - 3600000,
                        requesterPhone = "081234567801",
                        requesterId = "d99"
                    ),
                    SosRequestResponse(
                        id = "sos_mock_2",
                        requesterName = "Siti Rahma",
                        bloodType = "O-",
                        location = "RS Urip Sumoharjo",
                        quantity = 2,
                        notes = "Pasien demam berdarah membutuhkan trombosit golongan O-. Mohon bantuannya.",
                        timestamp = System.currentTimeMillis() - 7200000,
                        requesterPhone = "081234567802",
                        requesterId = "d98"
                    ),
                    SosRequestResponse(
                        id = "sos_mock_3",
                        requesterName = "Dewi Lestari",
                        bloodType = "AB-",
                        location = "RS Bumi Waras",
                        quantity = 4,
                        notes = "Dibutuhkan golongan darah AB- segera untuk pasien transfusi darah rutin.",
                        timestamp = System.currentTimeMillis() - 10800000,
                        requesterPhone = "081288776655",
                        requesterId = "d6"
                    ),
                    SosRequestResponse(
                        id = "sos_mock_4",
                        requesterName = "Budi Santoso",
                        bloodType = "A+",
                        location = "Kedaton",
                        quantity = 1,
                        notes = "Membutuhkan 1 kantong darah A+ untuk pasien melahirkan.",
                        timestamp = System.currentTimeMillis() - 14400000,
                        requesterPhone = "085211223344",
                        requesterId = "d3"
                    )
                )
                try {
                    mockSosList.forEach { request ->
                        apiService.saveFirebaseSosRequest(request.id, request)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mockSosList
            } else {
                map.values.toList().sortedByDescending { it.timestamp }
            }
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
            apiService.saveFirebaseSosHistory(request.requesterId, request.id, request)
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

    suspend fun deleteDonation(userId: String, donationId: String): Boolean {
        return try {
            val response = apiService.deleteFirebaseDonation(userId, donationId)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getChatMessages(roomId: String): List<ChatMessage> {
        return try {
            val map = apiService.getChatMessages(roomId)
            map?.values?.sortedBy { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendChatMessage(roomId: String, message: ChatMessage): ChatMessage {
        return apiService.sendChatMessage(roomId, message.id, message)
    }

    suspend fun getChatList(userId: String): List<ChatListEntry> {
        return try {
            val map = apiService.getChatList(userId)
            map?.values?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateChatListEntry(userId: String, contactId: String, entry: ChatListEntry): ChatListEntry {
        return apiService.updateChatListEntry(userId, contactId, entry)
    }
}
