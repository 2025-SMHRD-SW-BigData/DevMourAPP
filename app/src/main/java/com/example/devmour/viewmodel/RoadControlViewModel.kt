package com.example.devmour.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devmour.data.RoadControl
import com.example.devmour.repository.RoadControlRepository
import kotlinx.coroutines.launch

class RoadControlViewModel : ViewModel() {
    private val repository = RoadControlRepository()
    
    private val _roadControls = MutableLiveData<List<RoadControl>>()
    val roadControls: LiveData<List<RoadControl>> = _roadControls
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadRoadControls() {
        Log.d("RoadControlViewModel", "도로 통제 데이터 로드 시작")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("RoadControlViewModel", "API 호출 시작")
                
                val result = repository.getRoadControls()
                result.fold(
                    onSuccess = { roadControlList ->
                        Log.d("RoadControlViewModel", "API 호출 완료. 받은 도로 통제 수: ${roadControlList.size}")
                        
                        if (roadControlList.isNotEmpty()) {
                            Log.d("RoadControlViewModel", "첫 번째 도로 통제: ${roadControlList[0]}")
                        }
                        
                        _roadControls.value = roadControlList
                        Log.d("RoadControlViewModel", "도로 통제 데이터 로드 완료")
                    },
                    onFailure = { exception ->
                        Log.e("RoadControlViewModel", "도로 통제 데이터 로드 실패", exception)
                        _error.value = "도로 통제 데이터를 불러오는데 실패했습니다: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                Log.e("RoadControlViewModel", "도로 통제 데이터 로드 실패", e)
                _error.value = "도로 통제 데이터를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
