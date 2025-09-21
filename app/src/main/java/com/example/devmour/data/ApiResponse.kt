package com.example.devmour.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<T>,
    
    @SerializedName("message")
    val message: String?
)
