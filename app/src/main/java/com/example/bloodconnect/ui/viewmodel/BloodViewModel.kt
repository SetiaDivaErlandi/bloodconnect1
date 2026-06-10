package com.example.bloodconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.SosRequestResponse
import com.example.bloodconnect.data.model.DonationResponse
import com.example.bloodconnect.data.repository.BloodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class BloodViewModel(private val repository: BloodRepository) : ViewModel() {

    private val _bloodData = MutableStateFlow<UiState<BloodDataResponse>>(UiState.Loading)
    val bloodData: StateFlow<UiState<BloodDataResponse>> = _bloodData

    private val _sosRequests = MutableStateFlow<UiState<List<SosRequestResponse>>>(UiState.Loading)
    val sosRequests: StateFlow<UiState<List<SosRequestResponse>>> = _sosRequests

    private val _sosHistory = MutableStateFlow<UiState<List<SosRequestResponse>>>(UiState.Loading)
    val sosHistory: StateFlow<UiState<List<SosRequestResponse>>> = _sosHistory

    private val _donations = MutableStateFlow<UiState<List<DonationResponse>>>(UiState.Loading)
    val donations: StateFlow<UiState<List<DonationResponse>>> = _donations

    val bookmarks: StateFlow<Set<String>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val readHistory: StateFlow<Set<String>> = repository.readHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        fetchBloodData()
    }

    fun fetchBloodData() {
        viewModelScope.launch {
            _bloodData.value = UiState.Loading
            try {
                val response = repository.getBloodData()
                _bloodData.value = UiState.Success(response)
            } catch (e: Exception) {
                _bloodData.value = UiState.Error(
                    if (e is java.io.IOException || e is java.net.UnknownHostException) {
                        "Koneksi internet terputus. Silakan periksa koneksi Anda."
                    } else {
                        e.message ?: "Unknown Error"
                    }
                )
            }
        }
    }

    /**
     * Mengambil data SOS dan melakukan filtering berdasarkan userId.
     * Jika currentUserId diberikan, hanya menampilkan SOS milik user tersebut.
     */
    fun fetchSosRequests(currentUserId: String? = null) {
        viewModelScope.launch {
            _sosRequests.value = UiState.Loading
            try {
                val allRequests = repository.getSosRequests()
                val filteredList = if (currentUserId != null) {
                    allRequests.filter { it.requesterId == currentUserId }
                } else {
                    allRequests
                }
                _sosRequests.value = UiState.Success(filteredList)
            } catch (e: Exception) {
                _sosRequests.value = UiState.Error(
                    if (e is java.io.IOException || e is java.net.UnknownHostException) {
                        "Koneksi internet terputus. Silakan periksa koneksi Anda."
                    } else {
                        e.message ?: "Gagal memuat permintaan SOS"
                    }
                )
            }
        }
    }

    fun sendSosRequest(request: SosRequestResponse, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.saveSosRequest(request)
                if (result) {
                    fetchSosRequests(request.requesterId)
                    onSuccess()
                } else {
                    onError("Gagal mengirim SOS Alert.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Gagal mengirim SOS Alert.")
            }
        }
    }

    fun deleteSosRequest(id: String, currentUserId: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.deleteSosRequest(id)
                if (result) {
                    fetchSosRequests(currentUserId)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun completeSosRequest(request: SosRequestResponse, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.completeSosRequest(request)
                if (result) {
                    fetchSosRequests(request.requesterId)
                    fetchSosHistory(request.requesterId)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchSosHistory(userId: String) {
        viewModelScope.launch {
            _sosHistory.value = UiState.Loading
            try {
                val list = repository.getSosHistory(userId)
                _sosHistory.value = UiState.Success(list)
            } catch (e: Exception) {
                _sosHistory.value = UiState.Error("Gagal memuat riwayat SOS")
            }
        }
    }

    fun fetchDonations(userId: String) {
        viewModelScope.launch {
            _donations.value = UiState.Loading
            try {
                val list = repository.getDonations(userId)
                _donations.value = UiState.Success(list)
            } catch (e: Exception) {
                _donations.value = UiState.Error(
                    if (e is java.io.IOException || e is java.net.UnknownHostException) {
                        "Koneksi internet terputus. Silakan periksa koneksi Anda."
                    } else {
                        e.message ?: "Gagal memuat riwayat donor"
                    }
                )
            }
        }
    }

    fun submitDonation(userId: String, donation: DonationResponse, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.saveDonation(userId, donation)
                if (result) {
                    fetchDonations(userId)
                    onSuccess()
                } else {
                    onError("Gagal mengirim formulir donor.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Gagal mengirim formulir donor.")
            }
        }
    }

    fun toggleBookmark(title: String) {
        viewModelScope.launch {
            repository.toggleBookmark(title)
        }
    }

    fun addArticleToHistory(title: String) {
        viewModelScope.launch {
            repository.addArticleToHistory(title)
        }
    }
}
