package com.example.devmour

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.devmour.auth.SessionManager
import com.example.devmour.auth.SignOutHelper
import com.example.devmour.auth.LoginManager

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 지난 세션이 비정상 종료였다면 강제 로그아웃
        if (SessionManager.shouldForceLogout(this)) {
            // 로그인 상태가 있는 경우에만 강제 로그아웃 실행
            if (LoginManager.isLoggedIn(this)) {
                SignOutHelper.signOutAll(this) {
                    // 모든 OAuth 제공자 로그아웃 완료 후
                    LoginManager.logout(this) // 로컬 로그인 상태도 초기화
                    SessionManager.clearAll(this)
                    goLogin()
                }
                return
            } else {
                // 로그인 상태가 없으면 바로 메인으로
                goMain()
                return
            }
        }
        
        // 다음 종료를 대비해 일단 비정상 종료로 마킹(앱이 정상 종료 시 true로 바꿔줌)
        SessionManager.markCleanExit(this, false)
        
        // 항상 메인 화면으로 이동 (로그인 상태는 MainActivity에서 확인)
        goMain()
    }
    
    private fun goLogin() { 
        startActivity(Intent(this, LoginActivity::class.java))
        finish() 
    }
    
    private fun goMain() { 
        startActivity(Intent(this, MainActivity::class.java))
        finish() 
    }
}
