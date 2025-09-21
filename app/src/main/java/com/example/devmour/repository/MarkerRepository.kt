package com.example.devmour.repository

import com.example.devmour.api.RetrofitClient
import com.example.devmour.data.Road
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoadRepository {
    private val apiService = RetrofitClient.apiService
    
    suspend fun getRoads(): Result<List<Road>> = withContext(Dispatchers.IO) {
        try {
            // 실제 API 호출
            val response = apiService.getRoads()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoadsByGrade(grade: String): Result<List<Road>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoadsByGrade(grade)
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
