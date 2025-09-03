package com.example.devmour.api

import com.example.devmour.data.ApiResponse
import com.example.devmour.data.Road
import com.example.devmour.data.RoadControl
import com.example.devmour.data.ReportResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/roads")
    suspend fun getRoads(): ApiResponse<Road>
    
    @GET("api/roads")
    suspend fun getRoadsBySeverity(@Query("severity") severity: String): ApiResponse<Road>
    
    @GET("api/road-controls")
    suspend fun getRoadControls(): ApiResponse<RoadControl>
    
    @Multipart
    @POST("api/reports/submit")
    suspend fun submitReport(
        @Part("addr") addr: RequestBody,
        @Part("c_report_detail") cReportDetail: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part("c_reporter_name") cReporterName: RequestBody,
        @Part("c_reporter_phone") cReporterPhone: RequestBody,
        @Part cReportFile1: MultipartBody.Part?,
        @Part cReportFile2: MultipartBody.Part?,
        @Part cReportFile3: MultipartBody.Part?
    ): Response<ReportResponse>
}
