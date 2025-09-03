package com.example.devmour.data

// 광주시 위치 데이터 클래스
data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String, // "구" 또는 "동"
    val parent: String? = null // 동의 경우 상위 구 이름
)
