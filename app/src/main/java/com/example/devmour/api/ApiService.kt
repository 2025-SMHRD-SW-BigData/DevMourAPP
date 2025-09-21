package com.example.devmour.api

import com.example.devmour.data.ApiResponse
import com.example.devmour.data.GeocodingResponse
import com.example.devmour.data.Road
import com.example.devmour.data.RoadControl
import com.example.devmour.data.ReportResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 도로 마커 관련 API (mobile-markers.js)
    @GET("api/mobile/markers")
    suspend fun getRoads(): ApiResponse<Road>
    
    @GET("api/mobile/markers/grade/{grade}")
    suspend fun getRoadsByGrade(@Path("grade") grade: String): ApiResponse<Road>
    
    // 도로 통제 관련 API (mobile-road-controls.js)
    @GET("api/mobile/road-controls")
    suspend fun getRoadControls(): ApiResponse<RoadControl>
    
    @GET("api/mobile/road-controls/flood")
    suspend fun getFloodData(): ApiResponse<RoadControl>
    
    // 민원 제출 관련 API (mobile-reports.js) - S3 업로드 버전
    @Multipart
    @POST("api/mobile/reports/submit")
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

    // 민원 목록 조회 API
    @GET("api/mobile/reports/list")
    suspend fun getReportsList(): Response<ReportResponse>

    // 이미지 파일 조회 API (S3 URL을 직접 사용하므로 제거)
    // 클라이언트에서는 S3 URL을 직접 사용하여 이미지를 표시

    // 지오코딩 API (기존 유지)
    @GET("req/address")
    suspend fun searchAddress(
        @Query("service") service: String = "address",
        @Query("request") request: String = "getcoord",
        @Query("crs") crs: String = "epsg:4326",
        @Query("address") address: String,
        @Query("format") format: String = "json",
        @Query("type") type: String = "road",
        @Query("key") apiKey: String
    ): GeocodingResponse
}
