package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.devmour.auth.LoginManager
import com.example.devmour.auth.SessionManager
import com.example.devmour.auth.SignOutHelper
import com.kakao.sdk.user.UserApiClient

class LoginActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그아웃 버튼 리스너
        val btnLogout = findViewById<android.widget.Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            Log.d("LoginActivity2", "로그아웃 버튼 클릭됨")
            
            // 모든 OAuth 제공자에서 로그아웃
            SignOutHelper.signOutAll(this) {
                // 모든 OAuth 로그아웃 완료 후
                LoginManager.logout(this) // 로컬 로그인 상태도 초기화
                SessionManager.clearAll(this) // 세션 데이터도 초기화
                
                Log.d("LoginActivity2", "모든 로그아웃 완료, 로그인 화면으로 이동")
                
                // 로그아웃 후 로그인 화면으로 돌아가기
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 정상 종료로 마킹
        SessionManager.markCleanExit(this, true)
    }
}
