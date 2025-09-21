package com.example.devmour.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object LoginManager {
    private const val PREF_NAME = "login_preferences"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_LOGIN_TYPE = "login_type"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_LOGIN_TIME = "login_time"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // 로그인 상태 저장
    fun saveLoginState(context: Context, loginState: LoginState) {
        val prefs = getSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, loginState.isLoggedIn)
            putString(KEY_LOGIN_TYPE, loginState.loginType)
            putString(KEY_ACCESS_TOKEN, loginState.accessToken)
            putString(KEY_USER_ID, loginState.userId)
            putLong(KEY_LOGIN_TIME, loginState.loginTime)
        }.apply()
        
        Log.d("LoginManager", "로그인 상태 저장됨: ${loginState.loginType}")
    }

    // 로그인 상태 불러오기
    fun getLoginState(context: Context): LoginState {
        val prefs = getSharedPreferences(context)
        return LoginState(
            isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false),
            loginType = prefs.getString(KEY_LOGIN_TYPE, "") ?: "",
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, "") ?: "",
            userId = prefs.getString(KEY_USER_ID, "") ?: "",
            loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
        )
    }

    // 로그인 여부 확인
    fun isLoggedIn(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // 로그인 타입 확인
    fun getLoginType(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_LOGIN_TYPE, "") ?: ""
    }

    // 액세스 토큰 확인
    fun getAccessToken(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }

    // 로그아웃 (상태 초기화)
    fun logout(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().clear().apply()
        Log.d("LoginManager", "로그아웃 완료")
    }

    // 토큰 유효성 확인 (간단한 체크)
    fun isTokenValid(context: Context): Boolean {
        val loginState = getLoginState(context)
        if (!loginState.isLoggedIn) return false
        
        // 토큰이 비어있지 않고, 로그인 후 24시간 이내인지 확인
        val currentTime = System.currentTimeMillis()
        val loginTime = loginState.loginTime
        val validDuration = 24 * 60 * 60 * 1000L // 24시간
        
        return loginState.accessToken.isNotEmpty() && 
               (currentTime - loginTime) < validDuration
    }
}
