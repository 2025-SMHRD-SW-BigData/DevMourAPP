package com.example.devmour.alert

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devmour.MainActivity
import com.example.devmour.R
import com.example.devmour.ReportActivity
import com.example.devmour.alert.AlertAdapter
import com.example.devmour.alert_fixActivity
import com.example.devmour.data.db.repository.AppDatabase
import com.example.devmour.data.db.repository.RoadControlEntity
import com.example.devmour.data.RoadControlRepository
import com.example.devmour.databinding.ActivityMainAlertBinding
import com.example.devmour.network.ApiService
import com.example.devmour.util.showNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivityAlert : AppCompatActivity() {

    private lateinit var binding: ActivityMainAlertBinding
    private lateinit var adapter: AlertAdapter
    private lateinit var repository: RoadControlRepository
    
    // 네비게이션 바 관련 변수들
    private lateinit var ivNotification: ImageView
    private lateinit var ivMain: ImageView
    private lateinit var ivReport: ImageView
    private lateinit var btnNotification: LinearLayout
    private lateinit var btnMain: LinearLayout
    private lateinit var btnReport: LinearLayout

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://175.45.194.114:3001/") // Mobile API 서버 주소
        //"http://192.168.219.54:3001/" // 로컬 개발용
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val api = retrofit.create(ApiService::class.java)

    private val handler = Handler(Looper.getMainLooper())
    private val pollingInterval = 5000L // 5초
    private var lastRequestTime: String? = null // 마지막 요청 시간 추적 (첫 요청시 null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(applicationContext).roadControlDao()
        repository = RoadControlRepository(dao)

        adapter = AlertAdapter { item ->
            // 삭제 버튼 클릭 시 해당 아이템을 데이터베이스에서 삭제
            lifecycleScope.launch {
                repository.delete(item)
                Log.d("MainActivityAlert", "알림 삭제됨: ${item.control_desc}")
            }
        }
        binding.recyclerViewAlerts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAlerts.adapter = adapter

        binding.imageView2.setOnClickListener {
            val intent = Intent(this, alert_fixActivity::class.java)
            startActivity(intent)
        }

        // 네비게이션 바 초기화
        initNavigationBar()

        // 실제 데이터만 표시
        lifecycleScope.launch {
            repository.getAllControls().collectLatest { list ->
                Log.d("MainActivityAlert", "데이터베이스에서 가져온 데이터 개수: ${list.size}")
                if (list.isNotEmpty()) {
                    Log.d("MainActivityAlert", "첫 번째 데이터: ${list[0].control_desc}, ${list[0].control_addr}")
                }
                adapter.updateData(list)
            }
        }

        startRealTimePolling()
        startMidnightReset()
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
            // 현재 페이지이므로 아무 동작 안함
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

        // 알림내역 페이지 상태로 설정
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

    private fun startRealTimePolling() {
        handler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.getLatest(lastRequestTime)
                        Log.d("MainActivityAlert", "API 응답: $response")
                        if (response.newData && response.data != null) {
                            val control = response.data
                            Log.d("MainActivityAlert", "서버에서 받은 새로운 데이터: ${control.control_desc}, ${control.control_addr}")
                            
                            // 마지막 요청 시간 업데이트 (안드로이드 친화적 형식)
                            lastRequestTime = response.lastRequestTime ?: lastRequestTime
                            val controlTimeMillis = try {
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
                                    .parse(control.control_st_tm)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }

                            val entity = RoadControlEntity(
                                control_desc = control.control_desc,
                                control_addr = control.control_addr,
                                control_st_tm = controlTimeMillis,
                                control_type = null,
                                completed = null
                            )
                            repository.insert(entity)

                            runOnUiThread {
                                showNotification(control)
                            }
                        } else {
                            Log.d("MainActivityAlert", "새로운 데이터 없음")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                handler.postDelayed(this, pollingInterval)
            }
        })
    }

    private fun startMidnightReset() {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val todayMidnight = sdf.parse(sdf.format(now))?.time ?: now
        val delay = (todayMidnight + 24L * 60L * 60L * 1000L) - now // 다음 00시까지 남은 시간 계산

        handler.postDelayed(object : Runnable {
            override fun run() {
                adapter.updateData(emptyList()) // 화면 초기화
                handler.postDelayed(this, 24L * 60L * 60L * 1000L) // 매일 반복
            }
        }, delay)
    }
}
