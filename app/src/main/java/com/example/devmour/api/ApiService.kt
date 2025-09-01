package com.example.devmour.api

import com.example.devmour.data.ApiResponse
import com.example.devmour.data.Road
import com.example.devmour.data.RoadControl
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("api/roads")
    suspend fun getRoads(): ApiResponse<Road>
    
    @GET("api/roads")
    suspend fun getRoadsBySeverity(@Query("severity") severity: String): ApiResponse<Road>
    
    @GET("api/road-controls")
    suspend fun getRoadControls(): ApiResponse<RoadControl>
}
