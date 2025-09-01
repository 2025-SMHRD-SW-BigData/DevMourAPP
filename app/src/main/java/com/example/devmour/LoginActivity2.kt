package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // 로그아웃 버튼 리스너 (카카오 + 네이버)
        val btnLogout = findViewById<android.widget.Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            // 카카오 연결 끊기
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.e("test", "카카오 연결 끊기 실패", error)
                } else {
                    Log.i("test", "카카오 연결 끊기 성공. SDK에서 토큰 삭제 됨")
                }
            }
            
            // 네이버 로그아웃 (임시 비활성화)
            // NidOAuthLogin.logout()
            Log.i("test", "네이버 로그아웃 완료")
            
            // 로그아웃 후 로그인 화면으로 돌아가기
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}