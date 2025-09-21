package com.example.devmour.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.devmour.data.db.repository.AppDatabase
import com.example.devmour.data.db.repository.RoadControlEntity
import com.example.devmour.data.RoadControlRepository
import com.example.devmour.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class PollingWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

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

    override suspend fun doWork(): Result {
        return try {
            val response = withContext(Dispatchers.IO) { api.getLatest(null) }

            if (response.newData && response.data != null) {
                val control = response.data

                // String → Long 변환 (yyyy-MM-dd'T'HH:mm:ss)
                val controlTimeMillis = try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
                        .parse(control.control_st_tm)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                // DB 저장
                val dao = AppDatabase.getInstance(applicationContext).roadControlDao()
                val repository = RoadControlRepository(dao)
                repository.insert(
                    RoadControlEntity(
                        control_desc = control.control_desc,
                        control_addr = control.control_addr,
                        control_st_tm = controlTimeMillis,
                        control_type = null,   // API에 없으면 null
                        completed = null       // API에 없으면 null
                    )
                )

                // 알림 표시
                applicationContext.showNotification(control)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
