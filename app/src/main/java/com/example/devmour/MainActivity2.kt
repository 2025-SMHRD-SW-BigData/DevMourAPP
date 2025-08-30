package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.user.UserApiClient

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 카카오 로그아웃 버튼 클릭 리스너
        val btnLogout = findViewById<android.widget.Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            // 성공 여부와 관계 없이 토큰 삭제.
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e("test", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                } else {
                    Log.e("test", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                }
            }
            
            // 로그아웃 후 로그인 화면으로 돌아가기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}