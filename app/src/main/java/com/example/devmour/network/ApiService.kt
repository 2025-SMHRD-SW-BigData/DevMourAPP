package com.example.devmour.network

import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

data class ControlData(
    val id: Int,
    val control_desc: String,
    val control_st_tm: String, // 서버에서 오는 문자열
    val control_addr: String
) {
    // Room DB에 넣기 위해 Long 타입으로 변환
    fun toEntity(): com.example.devmour.data.db.repository.RoadControlEntity {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val timestamp = try {
            formatter.parse(control_st_tm)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return com.example.devmour.data.db.repository.RoadControlEntity(
            control_addr = control_addr,
            control_desc = control_desc,
            control_st_tm = timestamp
        )
    }
}

data class ApiResponse(
    val newData: Boolean,
    val data: ControlData?
)

interface ApiService {
    @GET("/latest")
    suspend fun getLatest(): ApiResponse
}
