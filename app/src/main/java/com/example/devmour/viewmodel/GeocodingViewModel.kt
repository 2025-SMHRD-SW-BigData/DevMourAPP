package com.example.devmour.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devmour.data.AddressSearchResult
import com.example.devmour.repository.GeocodingRepository
import kotlinx.coroutines.launch

class GeocodingViewModel : ViewModel() {
    private val repository = GeocodingRepository()

    private val _searchResult = MutableLiveData<AddressSearchResult?>()
    val searchResult: LiveData<AddressSearchResult?> = _searchResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // VWorld API 키 (실제 키로 교체 필요)
    private val VWORLD_API_KEY = "FCD5F7C6-9935-3861-AD01-41ACE25ED422"

    fun searchAddress(address: String) {
        if (address.isBlank()) {
            _error.value = "주소를 입력해주세요"
            return
        }

        Log.d("GeocodingViewModel", "주소 검색 시작: $address")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = repository.searchAddress(address, VWORLD_API_KEY)
                result.fold(
                    onSuccess = { searchResult ->
                        Log.d("GeocodingViewModel", "주소 검색 성공: ${searchResult.latitude}, ${searchResult.longitude}")
                        _searchResult.value = searchResult
                    },
                    onFailure = { exception ->
                        Log.e("GeocodingViewModel", "주소 검색 실패", exception)
                        _error.value = exception.message ?: "주소 검색에 실패했습니다"
                        _searchResult.value = null
                    }
                )
            } catch (e: Exception) {
                Log.e("GeocodingViewModel", "주소 검색 오류", e)
                _error.value = "주소 검색 중 오류가 발생했습니다"
                _searchResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _searchResult.value = null
        _error.value = null
    }
}