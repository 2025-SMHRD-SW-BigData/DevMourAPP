package com.example.devmour.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.devmour.alert.MainActivityAlert
import com.example.devmour.R
import com.example.devmour.network.ControlData

fun Context.showNotification(data: ControlData) {

    val channelId = "road_control_channel"
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // NotificationChannel 생성
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "도로 통제 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "도로 통제 관련 최신 알림"
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        manager.createNotificationChannel(channel)
    }

    // ✅ 알림 클릭 시 열릴 화면 지정
    val intent = Intent(this, MainActivityAlert::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        this, 0, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Notification 생성
    val notification = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_notification) // 알림 전용 단색 아이콘 필요
        .setContentTitle("도로 통제 안내")
        .setContentText("${data.control_addr} - ${data.control_desc} (${data.control_st_tm})")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent) // 👈 여기서 pendingIntent 사용
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .build()

    val notificationId = (System.currentTimeMillis() and 0xFFFFFFF).toInt()

    // 메인 스레드에서 알림 호출
    manager.notify(notificationId, notification)
}