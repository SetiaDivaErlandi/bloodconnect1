package com.example.bloodconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodconnect.data.remote.BloodDataResponse
import com.example.bloodconnect.data.repository.BloodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class BloodViewModel(private val repository: BloodRepository) : ViewModel() {

    private val _bloodData = MutableStateFlow<UiState<BloodDataResponse>>(UiState.Loading)
    val bloodData: StateFlow<UiState<BloodDataResponse>> = _bloodData

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
                _bloodData.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
