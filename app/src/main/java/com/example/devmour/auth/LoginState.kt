package com.example.devmour.auth

data class LoginState(
    val isLoggedIn: Boolean = false,
    val loginType: String = "", // "kakao", "naver", "google"
    val accessToken: String = "",
    val userId: String = "",
    val loginTime: Long = 0L
)
