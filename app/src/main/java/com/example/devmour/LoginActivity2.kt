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
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // LoginActivity2는 로그인 화면과 동일한 레이아웃을 사용
        // 로그아웃 기능은 필요시 다른 방법으로 구현
    }

    override fun onDestroy() {
        super.onDestroy()
        // 정상 종료로 마킹
        SessionManager.markCleanExit(this, true)
    }
}
