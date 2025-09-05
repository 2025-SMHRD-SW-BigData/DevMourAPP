package com.example.devmour

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.devmour.alert.MainActivityAlert

class alert_fixActivity : AppCompatActivity() {
    
    // 네비게이션 바 관련 변수들
    private lateinit var ivNotification: ImageView
    private lateinit var ivMain: ImageView
    private lateinit var ivReport: ImageView
    private lateinit var btnNotification: LinearLayout
    private lateinit var btnMain: LinearLayout
    private lateinit var btnReport: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert_fixmain)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 네비게이션 바 초기화
        initNavigationBar()
        
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
    
    private fun initNavigationBar() {
        // 네비게이션 바 요소들 초기화
        ivNotification = findViewById(R.id.ivNotification)
        ivMain = findViewById(R.id.ivMain)
        ivReport = findViewById(R.id.ivReport)
        btnNotification = findViewById(R.id.btnNotification)
        btnMain = findViewById(R.id.btnMain)
        btnReport = findViewById(R.id.btnReport)

        // 네비게이션 바 클릭 리스너 설정
        btnNotification.setOnClickListener {
            val intent = Intent(this, MainActivityAlert::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        btnMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        btnReport.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 환경설정 페이지 상태로 설정 (알림내역이 활성화)
        setNavigationBarState("notification")
    }

    private fun setNavigationBarState(currentPage: String) {
        // 모든 아이콘을 기본 상태(흰색)로 설정
        ivNotification.setImageResource(R.drawable.alarm_w)
        ivMain.setImageResource(R.drawable.main_w)
        ivReport.setImageResource(R.drawable.report_w)

        // 모든 텍스트를 기본 색상으로 설정
        (btnNotification as LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }
        (btnMain as LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }
        (btnReport as LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }

        // 현재 페이지에 따라 아이콘과 텍스트 색상 변경
        when (currentPage) {
            "notification" -> {
                ivNotification.setImageResource(R.drawable.alarm_b)
                (btnNotification as LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
            "main" -> {
                ivMain.setImageResource(R.drawable.main_b)
                (btnMain as LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
            "report" -> {
                ivReport.setImageResource(R.drawable.report_b)
                (btnReport as LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
        }
    }
}