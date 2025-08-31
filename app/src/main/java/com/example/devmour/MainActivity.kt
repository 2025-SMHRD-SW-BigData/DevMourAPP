package com.example.devmour

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.MapFragment
import com.example.devmour.data.Road as RoadData
import com.example.devmour.data.RoadControl as RoadControlData
import com.example.devmour.viewmodel.RoadViewModel
import com.example.devmour.viewmodel.RoadControlViewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var LOCATION_PERMISSION = 1004
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var roadViewModel: RoadViewModel
    private lateinit var roadControlViewModel: RoadControlViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    
    // 마커 리스트를 저장할 변수
    private val markers = mutableListOf<Marker>()
    private val controlMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate 시작")

        // ViewModel 초기화
        roadViewModel = ViewModelProvider(this)[RoadViewModel::class.java]
        roadControlViewModel = ViewModelProvider(this)[RoadControlViewModel::class.java]
        Log.d("MainActivity", "ViewModel 초기화 완료")

        // 도로 데이터 관찰 - onCreate에서 설정
        observeRoads()
        observeRoadControls()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION)
        val fragmentManager: FragmentManager = supportFragmentManager
        var mapFragment: MapFragment? = fragmentManager.findFragmentById(R.id.map) as MapFragment?
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit()
        }

        mapFragment!!.getMapAsync(this)
        Log.d("MainActivity", "지도 비동기 로드 요청")
        

    }
    private fun observeRoads() {
        Log.d("MainActivity", "=== Observer 설정 시작 ===")

        roadViewModel.roads.observe(this) { roadList ->
            Log.d("MainActivity", "=== LiveData Observer 트리거됨 ===")
            Log.d("MainActivity", "받은 도로 리스트: $roadList")
            Log.d("MainActivity", "도로 데이터 변경 감지. 도로 수: ${roadList?.size ?: 0}")

            if (roadList != null) {
                Log.d("MainActivity", "roadList.size: ${roadList.size}")
                Log.d("MainActivity", "roadList.isEmpty(): ${roadList.isEmpty()}")
            }
            // null 체크 추가
            if (roadList == null) {
                Log.w("MainActivity", "도로 리스트가 null임 - 처리중단")
                return@observe
            }

            // naverMap 초기화 확인
            if (!::naverMap.isInitialized) {
                Log.w("MainActivity", "naverMap이 아직 초기화되지 않음 - 처리 중단")
                return@observe
            }

            Log.d("MainActivity", "모든 조건 통과 - 도로 마커 처리 시작")

            // 기존 마커들 제거 (overlays.clear() 대신)
            Log.d("MainActivity", "기존 마커 제거: ${markers.size}개")
            markers.forEach { marker ->
                marker.map = null  // 지도에서 마커 제거
            }
            markers.clear()
            Log.d("MainActivity", "기존 마커들 제거 완료")


            // 도로 리스트가 비어있는지 확인
            if (roadList.isEmpty()) {
                Log.w("MainActivity", "받은 도로 리스트가 비어있음!")
                return@observe
            }
// 새로운 도로 마커들 추가
            roadList.forEachIndexed { index, roadData ->
                try {
                    val marker = Marker()

                    // 겹치지 않도록 약간씩 위치 조정
                    val offsetLat = (index % 5) * 0.0001  // 0.0001도씩 차이
                    val offsetLng = (index / 5) * 0.0001

                    marker.position = LatLng(
                        roadData.latitude + offsetLat,
                        roadData.longitude + offsetLng
                    )
                    marker.map = naverMap
                    marker.icon = MarkerIcons.BLACK
                    
                    // 심각도에 따라 다른 색상 적용
                    marker.iconTintColor = when (roadData.severityLevel) {
                        "위험" -> Color.RED
                        "경고" -> Color.rgb(255, 165, 0) // 주황색
                        "안전" -> Color.GREEN
                        else -> Color.GRAY
                    }
                    
                    marker.tag = "ROAD_${roadData.roadIdx}"

                    // 마커 클릭 이벤트 설정
                    marker.setOnClickListener { overlay ->
                        val severityText = when (roadData.severityLevel) {
                            "위험" -> "위험"
                            "경고" -> "경고"
                            "안전" -> "안전"
                            else -> "알 수 없음"
                        }
                        
                        val severityColor = when (roadData.severityLevel) {
                            "위험" -> Color.RED
                            "경고" -> Color.rgb(255, 165, 0)
                            "안전" -> Color.GREEN
                            else -> Color.GRAY
                        }
                        
                        val message = "도로 이상 현상 정보\n\n" +
                                "• 이상 유형: ${roadData.anomalyType}\n" +
                                "• 발견일시: ${roadData.detectedAt}\n" +
                                "• 심각도: $severityText"
                        
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("도로 이상 현상 정보")
                            .setMessage(message)
                            .setPositiveButton("확인") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                        
                        alertDialog.show()
                        
                        // 제목 텍스트 색상 설정
                        val titleTextView = alertDialog.findViewById<android.widget.TextView>(android.R.id.title)
                        titleTextView?.setTextColor(severityColor)
                        
                        true // 이벤트 처리 완료
                    }

                    markers.add(marker)

                    Log.d("MainActivity", "[$index] 도로 마커 위치: 원본=(${roadData.latitude}, ${roadData.longitude}), 조정=(${marker.position.latitude}, ${marker.position.longitude}), 심각도: ${roadData.severityLevel}")

                } catch (e: Exception) {
                    Log.e("MainActivity", "[$index] 도로 마커 생성 실패: ${e.message}")
                }
            }

            Log.d("MainActivity", "도로 마커 생성 완료. markers 리스트 크기: ${markers.size}")

// 실제로 지도에 표시된 마커 개수 확인 (가능하다면)
            val visibleMarkers = markers.count { it.map != null }
            Log.d("MainActivity", "지도에 실제 표시된 도로 마커 수: ${visibleMarkers}")

            Log.d("MainActivity", "최종: ${markers.size}개 도로 마커 추가 완료")
        }

        roadViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.e("MainActivity", "도로 로드 에러: $it")
            }
        }

        roadViewModel.isLoading.observe(this) { isLoading ->
            Log.d("MainActivity", "도로 로딩 상태: $isLoading")
        }
    }

    private fun observeRoadControls() {
        Log.d("MainActivity", "=== 도로 통제 Observer 설정 시작 ===")

        roadControlViewModel.roadControls.observe(this) { roadControlList ->
            Log.d("MainActivity", "=== LiveData Observer 트리거됨 ===")
            Log.d("MainActivity", "받은 도로 통제 리스트: $roadControlList")
            Log.d("MainActivity", "도로 통제 데이터 변경 감지. 도로 통제 수: ${roadControlList?.size ?: 0}")

            if (roadControlList != null) {
                Log.d("MainActivity", "roadControlList.size: ${roadControlList.size}")
                Log.d("MainActivity", "roadControlList.isEmpty(): ${roadControlList.isEmpty()}")
            }
            // null 체크 추가
            if (roadControlList == null) {
                Log.w("MainActivity", "도로 통제 리스트가 null임 - 처리중단")
                return@observe
            }

            // naverMap 초기화 확인
            if (!::naverMap.isInitialized) {
                Log.w("MainActivity", "naverMap이 아직 초기화되지 않음 - 처리 중단")
                return@observe
            }

            Log.d("MainActivity", "모든 조건 통과 - 도로 통제 마커 처리 시작")

            // 기존 통제 마커들 제거
            Log.d("MainActivity", "기존 통제 마커 제거: ${controlMarkers.size}개")
            controlMarkers.forEach { marker ->
                marker.map = null  // 지도에서 마커 제거
            }
            controlMarkers.clear()
            Log.d("MainActivity", "기존 통제 마커들 제거 완료")

            // 도로 통제 리스트가 비어있는지 확인
            if (roadControlList.isEmpty()) {
                Log.w("MainActivity", "받은 도로 통제 리스트가 비어있음!")
                return@observe
            }

            // 새로운 도로 통제 마커들 추가
            roadControlList.forEachIndexed { index, roadControlData ->
                try {
                    val marker = Marker()

                    // 겹치지 않도록 약간씩 위치 조정
                    val offsetLat = (index % 5) * 0.0001  // 0.0001도씩 차이
                    val offsetLng = (index / 5) * 0.0001

                    marker.position = LatLng(
                        roadControlData.latitude + offsetLat,
                        roadControlData.longitude + offsetLng
                    )
                    marker.map = naverMap
                    marker.icon = MarkerIcons.BLACK
                    
                    // 도로 통제는 보라색으로 표시
                    marker.iconTintColor = Color.rgb(128, 0, 128) // 보라색
                    
                    marker.tag = "CONTROL_${roadControlData.controlIdx}"

                    // 마커 클릭 이벤트 설정 - 다이얼로그 표시
                    marker.setOnClickListener { overlay ->
                        val message = "🚧 도로 통제 구역\n\n" +
                                "📝 설명: ${roadControlData.controlDesc}\n" +
                                "🕐 시작: ${roadControlData.controlStTm}\n" +
                                "🕐 종료: ${roadControlData.controlEdTm ?: "미정"}\n" +
                                "📍 주소: ${roadControlData.controlAddr}\n" +
                                "🏗️ 통제 유형: ${roadControlData.controlType}\n" +
                                "✅ 완료 여부: ${if (roadControlData.completed == "Y") "완료" else "진행중"}"
                        
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("🚧 도로 통제 구역 정보")
                            .setMessage(message)
                            .setPositiveButton("확인") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                        
                        alertDialog.show()
                        
                        // 제목 텍스트 색상 설정 (보라색)
                        val titleTextView = alertDialog.findViewById<android.widget.TextView>(android.R.id.title)
                        titleTextView?.setTextColor(Color.rgb(128, 0, 128))
                        
                        true // 이벤트 처리 완료
                    }

                    controlMarkers.add(marker)

                    Log.d("MainActivity", "[$index] 도로 통제 마커 위치: 원본=(${roadControlData.latitude}, ${roadControlData.longitude}), 조정=(${marker.position.latitude}, ${marker.position.longitude})")

                } catch (e: Exception) {
                    Log.e("MainActivity", "[$index] 도로 통제 마커 생성 실패: ${e.message}")
                }
            }

            Log.d("MainActivity", "도로 통제 마커 생성 완료. controlMarkers 리스트 크기: ${controlMarkers.size}")

            // 실제로 지도에 표시된 마커 개수 확인
            val visibleMarkers = controlMarkers.count { it.map != null }
            Log.d("MainActivity", "지도에 실제 표시된 도로 통제 마커 수: ${visibleMarkers}")

            Log.d("MainActivity", "최종: ${controlMarkers.size}개 도로 통제 마커 추가 완료")
        }

        roadControlViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.e("MainActivity", "도로 통제 로드 에러: $it")
            }
        }

        roadControlViewModel.isLoading.observe(this) { isLoading ->
            Log.d("MainActivity", "도로 통제 로딩 상태: $isLoading")
        }
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        Log.d("MainActivity", "지도 준비 완료")
        naverMap = map

        // 잠시 기다린 후 지도 상태 확인
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d("MainActivity", "지도 준비 3초 후 상태 확인")
            // 여기서 빨간 마커들이 이미 있는지 확인할 수 있음
            Log.d("MainActivity", "현재 우리가 관리하는 마커 수: ${markers.size}")
        }, 3000)

        // 카메라 설정 (광주 지역으로 설정)
        val cameraPosition = CameraPosition(
            LatLng(35.1595, 126.8526),
            16.0,
            20.0,
            0.0
        )
        naverMap.cameraPosition = cameraPosition
        Log.d("MainActivity", "카메라 위치 설정 완료")
        
        // 지도 클릭 이벤트 비활성화 (마커 추가/삭제 기능 제거)
        naverMap.setOnMapClickListener { point, coord ->
            Log.d("MainActivity", "지도 클릭: ${coord.latitude}, ${coord.longitude}")
        }
        
        naverMap.locationSource = locationSource
        ActivityCompat.requestPermissions(this, PERMISSION, LOCATION_PERMISSION)
        
        // 지도가 준비되면 도로 데이터 로드
        Log.d("MainActivity", "도로 데이터 로드 시작")
        roadViewModel.loadRoads()
        
        // 도로 통제 데이터 로드
        Log.d("MainActivity", "도로 통제 데이터 로드 시작")
        roadControlViewModel.loadRoadControls()

        // 잠시 후 LiveData 값을 강제로 확인
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d("MainActivity", "현재 LiveData 값 확인: ${roadViewModel.roads.value?.size}")
            roadViewModel.roads.value?.let {
                Log.d("MainActivity", "LiveData에 데이터가 있지만 Observer가 실행되지 않음")
            }
        }, 1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when {
            requestCode != LOCATION_PERMISSION -> {
                return
            }
            else -> {
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)

                if (!locationSource.isActivated) {
                    naverMap.locationTrackingMode = LocationTrackingMode.None
                } else {
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                }
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}