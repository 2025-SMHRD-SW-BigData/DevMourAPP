package com.example.devmour.repository

import com.example.devmour.data.AddressSearchResult
import com.example.devmour.data.GeocodingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface VWorldApiService {
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

class GeocodingRepository {
    private val vworldApiService: VWorldApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vworld.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        vworldApiService = retrofit.create(VWorldApiService::class.java)
    }

    suspend fun searchAddress(address: String, apiKey: String): Result<AddressSearchResult> = withContext(Dispatchers.IO) {
        try {
            val response = vworldApiService.searchAddress(
                address = address,
                apiKey = apiKey
            )

            if (response.response.status == "OK" && response.response.result != null) {
                val point = response.response.result.point
                if (point != null) {
                    val result = AddressSearchResult(
                        address = address,
                        latitude = point.y,
                        longitude = point.x
                    )
                    Result.success(result)
                } else {
                    Result.failure(Exception("검색 결과가 없습니다"))
                }
            } else {
                Result.failure(Exception("주소 검색에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}