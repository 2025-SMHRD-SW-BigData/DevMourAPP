package com.example.devmour.ui.alert

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.219.54:3003") // Node.js 서버 주소
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val api = retrofit.create(ApiService::class.java)

    private val handler = Handler(Looper.getMainLooper())
    private val pollingInterval = 2000L // 2초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(applicationContext).roadControlDao()
        repository = RoadControlRepository(dao)

        adapter = AlertAdapter()
        binding.recyclerViewAlerts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAlerts.adapter = adapter

        binding.imageView2.setOnClickListener {
            val intent = Intent(this, alert_fixActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repository.getAllControls().collectLatest { list ->
                adapter.updateData(list)
            }
        }

        startRealTimePolling()
        startMidnightReset()
    }

    private fun startRealTimePolling() {
        handler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.getLatest()
                        Log.d("MainActivityAlert", "API 응답: $response")
                        if (response.newData && response.data != null) {
                            val control = response.data
                            Log.d("MainActivityAlert", "서버에서 받은 데이터: ${control.control_desc}, ${control.control_addr}")
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
