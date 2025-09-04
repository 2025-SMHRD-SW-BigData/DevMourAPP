package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.devmour.ui.alert.MainActivityAlert

class alert_fixActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert_fixmain)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 🔹 imageView4 클릭 시 MainActivityAlert로 이동
        val backButton = findViewById<ImageView>(R.id.imageView4)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivityAlert::class.java)
            // 뒤로가기처럼 동작하려면 FLAG 추가 가능
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish() // 현재 Activity 종료
        }
    }
}