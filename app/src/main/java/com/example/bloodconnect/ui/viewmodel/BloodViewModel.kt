package com.example.bloodconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.SosRequestResponse
import com.example.bloodconnect.data.model.DonationResponse
import com.example.bloodconnect.data.model.ChatMessage
import com.example.bloodconnect.data.model.ChatListEntry
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

class BloodViewModel(val repository: BloodRepository) : ViewModel() {

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
        fetchSosRequests()
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

    fun deleteDonation(userId: String, donationId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDonation(userId, donationId)
                if (result) {
                    fetchDonations(userId)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _chatList = MutableStateFlow<List<ChatListEntry>>(emptyList())
    val chatList: StateFlow<List<ChatListEntry>> = _chatList

    fun fetchChatMessages(roomId: String) {
        viewModelScope.launch {
            try {
                val list = repository.getChatMessages(roomId)
                _chatMessages.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendChatMessage(
        roomId: String,
        senderId: String,
        senderName: String,
        senderImageUrl: String,
        recipientId: String,
        recipientName: String,
        recipientImageUrl: String,
        text: String
    ) {
        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                val messageId = "msg_$timestamp"
                val chatMessage = ChatMessage(
                    id = messageId,
                    senderId = senderId,
                    senderName = senderName,
                    text = text,
                    timestamp = timestamp
                )
                repository.sendChatMessage(roomId, chatMessage)

                val myEntry = ChatListEntry(
                    contactId = recipientId,
                    contactName = recipientName,
                    contactImageUrl = recipientImageUrl,
                    lastMessage = text,
                    timestamp = timestamp,
                    unreadCount = 0
                )
                repository.updateChatListEntry(senderId, recipientId, myEntry)

                val recipientChats = repository.getChatList(recipientId)
                val existingEntry = recipientChats.find { it.contactId == senderId }
                val newUnread = (existingEntry?.unreadCount ?: 0) + 1

                val partnerEntry = ChatListEntry(
                    contactId = senderId,
                    contactName = senderName,
                    contactImageUrl = senderImageUrl,
                    lastMessage = text,
                    timestamp = timestamp,
                    unreadCount = newUnread
                )
                repository.updateChatListEntry(recipientId, senderId, partnerEntry)

                fetchChatMessages(roomId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchChatList(userId: String) {
        viewModelScope.launch {
            try {
                val list = repository.getChatList(userId)
                _chatList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearUnreadCount(userId: String, contactId: String) {
        viewModelScope.launch {
            try {
                val list = repository.getChatList(userId)
                val existing = list.find { it.contactId == contactId }
                if (existing != null && existing.unreadCount > 0) {
                    val updated = existing.copy(unreadCount = 0)
                    repository.updateChatListEntry(userId, contactId, updated)
                    fetchChatList(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
