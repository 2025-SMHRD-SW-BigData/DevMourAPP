package com.example.devmour.data

import com.google.gson.annotations.SerializedName

data class Road(
    @SerializedName("total_idx")
    val totalIdx: Int,
    
    @SerializedName("cctv_idx")
    val cctvIdx: Int,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("total_score")
    val totalScore: Double,
    
    @SerializedName("detected_at")
    val detectedAt: String,
    
    @SerializedName("marker_icon")
    val markerIcon: String,
    
    @SerializedName("grade")
    val grade: String
)
