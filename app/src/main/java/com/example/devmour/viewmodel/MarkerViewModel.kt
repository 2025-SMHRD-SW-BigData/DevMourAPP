package com.example.devmour.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devmour.data.Road
import com.example.devmour.repository.RoadRepository
import kotlinx.coroutines.launch

class RoadViewModel : ViewModel() {
    private val repository = RoadRepository()
    
    private val _roads = MutableLiveData<List<Road>>()
    val roads: LiveData<List<Road>> = _roads
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 캐시된 데이터 저장
    private var cachedRoads: List<Road>? = null
    private var lastLoadTime: Long = 0
    private val CACHE_DURATION = 30000L // 30초 캐시

    fun loadRoads() {

        // 캐시된 데이터가 있고 30초 이내라면 캐시 사용
        val currentTime = System.currentTimeMillis()
        cachedRoads?.let { cached -> // safe call 사용
            if ((currentTime - lastLoadTime) < CACHE_DURATION) {
                Log.d("RoadViewModel", "캐시된 데이터 사용")
                _roads.value = cached
                return
            }
        }
        Log.d("RoadViewModel", "도로 데이터 로드 시작")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("RoadViewModel", "API 호출 시작")
                
                val result = repository.getRoads()
                result.fold(
                    onSuccess = { roadList ->
                        Log.d("RoadViewModel", "API 호출 완료. 받은 도로 수: ${roadList.size}")
                        
                        if (roadList.isNotEmpty()) {
                            Log.d("RoadViewModel", "첫 번째 도로: ${roadList[0]}")
                        }

                        // 캐시 업데이트 (만약 캐싱을 적용했다면)
                        cachedRoads = roadList
                        lastLoadTime = currentTime

                        _roads.value = roadList
                        Log.d("RoadViewModel", "도로 데이터 로드 완료")
                    },
                    onFailure = { exception ->
                        Log.e("RoadViewModel", "도로 데이터 로드 실패", exception)
                        _error.value = "도로 데이터를 불러오는데 실패했습니다: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                Log.e("RoadViewModel", "도로 데이터 로드 실패", e)
                _error.value = "도로 데이터를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
