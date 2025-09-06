package com.example.devmour.data

data class GeocodingResponse(
    val response: GeocodingResult
)

data class GeocodingResult(
    val service: ServiceInfo,
    val status: String,
    val input: InputInfo,
    val result: ResultInfo?
)

data class ServiceInfo(
    val name: String,
    val version: String,
    val operation: String,
    val time: String
)

data class InputInfo(
    val point: PointInfo,
    val crs: String,
    val type: String
)

data class PointInfo(
    val x: String,
    val y: String
)

data class ResultInfo(
    val crs: String,
    val point: GeoPoint
)

data class GeoPoint(
    val x: Double,
    val y: Double
)

// 검색 결과를 위한 데이터 클래스
data class AddressSearchResult(
    val address: String,
    val latitude: Double,
    val longitude: Double
)