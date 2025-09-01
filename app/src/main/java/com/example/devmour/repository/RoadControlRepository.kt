package com.example.devmour.repository

import com.example.devmour.api.RetrofitClient
import com.example.devmour.data.RoadControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoadControlRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun getRoadControls(): Result<List<RoadControl>> = withContext(Dispatchers.IO) {
        try {
            // 실제 API 호출
            val response = apiService.getRoadControls()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
