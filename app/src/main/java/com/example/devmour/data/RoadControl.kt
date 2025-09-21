package com.example.devmour.data

import com.google.gson.annotations.SerializedName

data class RoadControl(
    @SerializedName("control_idx")
    val controlIdx: Int,
    
    @SerializedName("pred_idx")
    val predIdx: Int,
    
    @SerializedName("control_desc")
    val controlDesc: String,
    
    @SerializedName("control_st_tm")
    val controlStTm: String,
    
    @SerializedName("control_ed_tm")
    val controlEdTm: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("road_idx")
    val roadIdx: Int,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lon")
    val longitude: Double,
    
    @SerializedName("control_addr")
    val controlAddr: String,
    
    @SerializedName("control_type")
    val controlType: String,
    
    @SerializedName("completed")
    val completed: String
)
