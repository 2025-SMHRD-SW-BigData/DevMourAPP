package com.example.devmour.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val PREF_NAME = "session_preferences"
    private const val KEY_CLEAN_EXIT = "clean_exit"
    private const val KEY_LAST_EXIT_TIME = "last_exit_time"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    // 정상 종료 여부 마킹
    fun markCleanExit(context: Context, isClean: Boolean) {
        val prefs = getSharedPreferences(context)
        prefs.edit().apply {
            putBoolean(KEY_CLEAN_EXIT, isClean)
            putLong(KEY_LAST_EXIT_TIME, System.currentTimeMillis())
        }.apply()
        
        Log.d("SessionManager", "세션 종료 상태 마킹: clean=$isClean")
    }
    
    // 강제 로그아웃이 필요한지 확인
    fun shouldForceLogout(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        val wasCleanExit = prefs.getBoolean(KEY_CLEAN_EXIT, false)
        val lastExitTime = prefs.getLong(KEY_LAST_EXIT_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        
        // 앱 종료 시에는 정상/비정상 상관없이 무조건 강제 로그아웃
        Log.d("SessionManager", "앱 종료 감지 - 강제 로그아웃 실행 (clean=$wasCleanExit, timeDiff=${currentTime - lastExitTime}ms)")
        return true
    }
    
    // 모든 세션 데이터 초기화
    fun clearAll(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().clear().apply()
        Log.d("SessionManager", "모든 세션 데이터 초기화 완료")
    }
}
