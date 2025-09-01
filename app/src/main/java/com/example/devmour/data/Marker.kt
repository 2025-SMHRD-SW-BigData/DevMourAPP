package com.example.devmour.data

import com.google.gson.annotations.SerializedName

data class Road(
    @SerializedName("road_idx")
    val roadIdx: Int,
    
    @SerializedName("anomaly_type")
    val anomalyType: String,
    
    @SerializedName("severity_level")
    val severityLevel: String,
    
    @SerializedName("detected_at")
    val detectedAt: String,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("cctv_idx")
    val cctvIdx: Int,
    
    @SerializedName("img_file")
    val imgFile: String,
    
    @SerializedName("v_idx")
    val vIdx: Int,
    
    @SerializedName("is_check")
    val isCheck: String,
    
    @SerializedName("admin_id")
    val adminId: String,
    
    @SerializedName("check_date")
    val checkDate: String,
    
    @SerializedName("is_resolved")
    val isResolved: String,
    
    @SerializedName("resolved_at")
    val resolvedAt: String,
    
    @SerializedName("by_citizen")
    val byCitizen: String
)
