package com.example.devmour

import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.ArrayDeque
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.MapFragment
import com.example.devmour.data.Road as RoadData
import com.example.devmour.data.RoadControl as RoadControlData
import com.example.devmour.viewmodel.RoadViewModel
import com.example.devmour.viewmodel.RoadControlViewModel

// 광주시 위치 데이터 클래스
data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String, // "구" 또는 "동"
    val parent: String? = null // 동의 경우 상위 구 이름
)

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var searchEditText: EditText

    private var LOCATION_PERMISSION = 1004
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var roadViewModel: RoadViewModel
    private lateinit var roadControlViewModel: RoadControlViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    
    // 네비게이션 바 요소들
    private lateinit var btnNotification: android.view.View
    private lateinit var btnMain: android.view.View
    private lateinit var btnReport: android.view.View
    
    // 마커 리스트를 저장할 변수
    private val markers = mutableListOf<Marker>()
    private val controlMarkers = mutableListOf<Marker>()
    private val locationMarkers = mutableListOf<Marker>() // 위치 검색 마커
    private val overlayImageCache = mutableMapOf<Int, OverlayImage>()
    
    // 광주시 위치 데이터 (하드코딩) - 실제 좌표 사용
    private val gwangjuLocations = listOf(
        // 광산구 (실제 중심 좌표)
        LocationData("광산구", 35.1392, 126.7940, "구"),
        LocationData("도산동", 35.1450, 126.7850, "동", "광산구"),
        LocationData("신흥동", 35.1350, 126.7850, "동", "광산구"),
        LocationData("어룡동", 35.1400, 126.8000, "동", "광산구"),
        LocationData("우산동", 35.1500, 126.7900, "동", "광산구"),
        LocationData("운남동", 35.1300, 126.8000, "동", "광산구"),
        LocationData("월곡동", 35.1450, 126.8100, "동", "광산구"),
        LocationData("월봉동", 35.1350, 126.8100, "동", "광산구"),
        LocationData("임곡동", 35.1500, 126.7800, "동", "광산구"),
        LocationData("장덕동", 35.1400, 126.7900, "동", "광산구"),
        LocationData("정광동", 35.1300, 126.7900, "동", "광산구"),
        LocationData("평동", 35.1450, 126.8000, "동", "광산구"),
        LocationData("하남동", 35.1350, 126.8000, "동", "광산구"),
        LocationData("황룡동", 35.1500, 126.8100, "동", "광산구"),
        
        // 남구 (실제 중심 좌표)
        LocationData("남구", 35.1333, 126.9000, "구"),
        LocationData("구동", 35.1400, 126.8950, "동", "남구"),
        LocationData("노대동", 35.1300, 126.8950, "동", "남구"),
        LocationData("대촌동", 35.1350, 126.9050, "동", "남구"),
        LocationData("덕남동", 35.1250, 126.9050, "동", "남구"),
        LocationData("도천동", 35.1400, 126.9050, "동", "남구"),
        LocationData("방림동", 35.1300, 126.9050, "동", "남구"),
        LocationData("백운동", 35.1350, 126.8950, "동", "남구"),
        LocationData("봉선동", 35.1250, 126.8950, "동", "남구"),
        LocationData("사직동", 35.1400, 126.9000, "동", "남구"),
        LocationData("송하동", 35.1300, 126.9000, "동", "남구"),
        LocationData("양림동", 35.1350, 126.9000, "동", "남구"),
        LocationData("월산동", 35.1250, 126.9000, "동", "남구"),
        LocationData("주월동", 35.1400, 126.9100, "동", "남구"),
        LocationData("지석동", 35.1300, 126.9100, "동", "남구"),
        LocationData("진월동", 35.1350, 126.9100, "동", "남구"),
        LocationData("치평동", 35.1250, 126.9100, "동", "남구"),
        LocationData("하동", 35.1400, 126.9150, "동", "남구"),
        LocationData("효덕동", 35.1300, 126.9150, "동", "남구"),
        
        // 동구 (실제 중심 좌표)
        LocationData("동구", 35.1544, 126.9233, "구"),
        LocationData("계림동", 35.1600, 126.9180, "동", "동구"),
        LocationData("광산동", 35.1500, 126.9180, "동", "동구"),
        LocationData("내남동", 35.1550, 126.9280, "동", "동구"),
        LocationData("대인동", 35.1450, 126.9280, "동", "동구"),
        LocationData("동명동", 35.1600, 126.9280, "동", "동구"),
        LocationData("불로동", 35.1500, 126.9280, "동", "동구"),
        LocationData("산수동", 35.1550, 126.9180, "동", "동구"),
        LocationData("서석동", 35.1450, 126.9180, "동", "동구"),
        LocationData("소태동", 35.1600, 126.9230, "동", "동구"),
        LocationData("수기동", 35.1500, 126.9230, "동", "동구"),
        LocationData("용산동", 35.1550, 126.9230, "동", "동구"),
        LocationData("지산동", 35.1450, 126.9230, "동", "동구"),
        LocationData("지원동", 35.1600, 126.9330, "동", "동구"),
        LocationData("충장로", 35.1500, 126.9330, "동", "동구"),
        LocationData("학동", 35.1550, 126.9330, "동", "동구"),
        LocationData("황금동", 35.1450, 126.9330, "동", "동구"),
        
        // 북구 (실제 중심 좌표)
        LocationData("북구", 35.1747, 126.9120, "구"),
        LocationData("각화동", 35.1800, 126.9070, "동", "북구"),
        LocationData("건국동", 35.1700, 126.9070, "동", "북구"),
        LocationData("금곡동", 35.1750, 126.9170, "동", "북구"),
        LocationData("남동", 35.1650, 126.9170, "동", "북구"),
        LocationData("대촌동", 35.1800, 126.9170, "동", "북구"),
        LocationData("덕의동", 35.1700, 126.9170, "동", "북구"),
        LocationData("동림동", 35.1750, 126.9070, "동", "북구"),
        LocationData("두암동", 35.1650, 126.9070, "동", "북구"),
        LocationData("망월동", 35.1800, 126.9120, "동", "북구"),
        LocationData("매곡동", 35.1700, 126.9120, "동", "북구"),
        LocationData("문흥동", 35.1750, 126.9120, "동", "북구"),
        LocationData("본촌동", 35.1650, 126.9120, "동", "북구"),
        LocationData("사직동", 35.1800, 126.9220, "동", "북구"),
        LocationData("삼각동", 35.1700, 126.9220, "동", "북구"),
        LocationData("상무동", 35.1750, 126.9220, "동", "북구"),
        LocationData("생용동", 35.1650, 126.9220, "동", "북구"),
        LocationData("수곡동", 35.1800, 126.9270, "동", "북구"),
        LocationData("신안동", 35.1700, 126.9270, "동", "북구"),
        LocationData("양산동", 35.1750, 126.9270, "동", "북구"),
        LocationData("연제동", 35.1650, 126.9270, "동", "북구"),
        LocationData("오룡동", 35.1800, 126.9320, "동", "북구"),
        LocationData("오치동", 35.1700, 126.9320, "동", "북구"),
        LocationData("용두동", 35.1750, 126.9320, "동", "북구"),
        LocationData("운암동", 35.1650, 126.9320, "동", "북구"),
        LocationData("월출동", 35.1800, 126.9370, "동", "북구"),
        LocationData("유동", 35.1700, 126.9370, "동", "북구"),
        LocationData("임동", 35.1750, 126.9370, "동", "북구"),
        LocationData("장등동", 35.1650, 126.9370, "동", "북구"),
        LocationData("중흥동", 35.1800, 126.9420, "동", "북구"),
        LocationData("지야동", 35.1700, 126.9420, "동", "북구"),
        LocationData("진월동", 35.1750, 126.9420, "동", "북구"),
        LocationData("청풍동", 35.1650, 126.9420, "동", "북구"),
        LocationData("충효동", 35.1800, 126.9470, "동", "북구"),
        LocationData("태령동", 35.1700, 126.9470, "동", "북구"),
        LocationData("풍향동", 35.1750, 126.9470, "동", "북구"),
        LocationData("화암동", 35.1650, 126.9470, "동", "북구"),
        
        // 서구 (실제 중심 좌표)
        LocationData("서구", 35.1267, 126.8667, "구"),
        LocationData("광천동", 35.1320, 126.8610, "동", "서구"),
        LocationData("내방동", 35.1220, 126.8610, "동", "서구"),
        LocationData("농성동", 35.1270, 126.8710, "동", "서구"),
        LocationData("덕흥동", 35.1170, 126.8710, "동", "서구"),
        LocationData("마륵동", 35.1320, 126.8710, "동", "서구"),
        LocationData("매월동", 35.1220, 126.8710, "동", "서구"),
        LocationData("벽진동", 35.1270, 126.8760, "동", "서구"),
        LocationData("비아동", 35.1170, 126.8760, "동", "서구"),
        LocationData("사호동", 35.1320, 126.8760, "동", "서구"),
        LocationData("서창동", 35.1220, 126.8760, "동", "서구"),
        LocationData("세하동", 35.1270, 126.8810, "동", "서구"),
        LocationData("송정동", 35.1170, 126.8810, "동", "서구"),
        LocationData("신촌동", 35.1320, 126.8810, "동", "서구"),
        LocationData("양동", 35.1220, 126.8810, "동", "서구"),
        LocationData("양림동", 35.1270, 126.8860, "동", "서구"),
        LocationData("염주동", 35.1170, 126.8860, "동", "서구"),
        LocationData("오정동", 35.1320, 126.8860, "동", "서구"),
        LocationData("용두동", 35.1220, 126.8860, "동", "서구"),
        LocationData("유촌동", 35.1270, 126.8910, "동", "서구"),
        LocationData("월산동", 35.1170, 126.8910, "동", "서구"),
        LocationData("월정동", 35.1320, 126.8910, "동", "서구"),
        LocationData("유덕동", 35.1220, 126.8910, "동", "서구"),
        LocationData("진월동", 35.1270, 126.8960, "동", "서구"),
        LocationData("치평동", 35.1170, 126.8960, "동", "서구"),
        LocationData("풍암동", 35.1320, 126.8960, "동", "서구"),
        LocationData("하동", 35.1220, 126.8960, "동", "서구"),
        LocationData("화정동", 35.1270, 126.8960, "동", "서구"),
        LocationData("환덕동", 35.1170, 126.8960, "동", "서구")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        Log.d("MainActivity", "onCreate 시작")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        // ViewModel 초기화
        roadViewModel = ViewModelProvider(this)[RoadViewModel::class.java]
        roadControlViewModel = ViewModelProvider(this)[RoadControlViewModel::class.java]
        Log.d("MainActivity", "ViewModel 초기화 완료")

        // UI 초기화
        initNavigationBar()

        // 데이터 관찰 설정
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
    
    private fun initNavigationBar() {
        btnNotification = findViewById(R.id.btnNotification)
        btnMain = findViewById(R.id.btnMain)
        btnReport = findViewById(R.id.btnReport)
        
        // 알림 버튼 클릭
        btnNotification.setOnClickListener {
            // TODO: 알림 화면으로 이동
            android.widget.Toast.makeText(this, "알림 화면", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // 메인화면 버튼 클릭 (현재 화면이므로 아무 동작 안함)
        btnMain.setOnClickListener {
            // 현재 메인화면이므로 아무 동작 안함
        }
        
        // 민원접수 버튼 클릭
        btnReport.setOnClickListener {
            // TODO: 민원접수 화면으로 이동
            android.widget.Toast.makeText(this, "민원접수 화면", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // 현재 메인화면이므로 메인화면 아이콘 텍스트 색상을 강조
        (btnMain as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#2f354f"))
            }
        }
        
        // 기존 검색 버튼을 위치 검색 기능과 연결
        val searchButton = findViewById<android.widget.Button>(R.id.btnSearch)
        val searchEditText = findViewById<android.widget.EditText>(R.id.etSearch)
        
        searchButton?.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                val searchResults = searchLocations(searchQuery)
                if (searchResults.isNotEmpty()) {
                    // 첫 번째 검색 결과로 이동
                    moveToLocation(searchResults.first())
                    android.widget.Toast.makeText(this, "${searchResults.first().name}으로 이동했습니다", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this, "검색 결과가 없습니다", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, "검색어를 입력해주세요", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // 엔터키로 검색
        searchEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick()
                true
            } else {
                false
            }
        }
    }
    
    private fun observeRoads() {
        Log.d("MainActivity", "=== Observer 설정 시작 ===")

        roadViewModel.roads.observe(this) { roadList ->
            Log.d("MainActivity", "=== 도로 Observer 시작 ===")
            Log.d("MainActivity", "현재 통제 마커 수: ${controlMarkers.size}")
            Log.d("MainActivity", "받은 도로 리스트: ${roadList?.size}개")

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

            // 기존 마커들 제거 (overlays.clear() 대신)
            Log.d("MainActivity", "도로 마커 제거: ${markers.size}개")
            markers.forEach { marker ->
                marker.map = null  // 지도에서 마커 제거
            }
            markers.clear()
            Log.d("MainActivity", "도로 마커들 제거 완료")

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
                    val offsetLat = (index % 5) * 0.0001
                    val offsetLng = (index / 5) * 0.0001

                    marker.position = LatLng(
                        roadData.latitude + offsetLat,
                        roadData.longitude + offsetLng
                    )
                    marker.map = naverMap

                    // 아이콘 이미지를 drawable 리소스로 교체
                    val iconResId = when (roadData.anomalyType) {
                        "침수" -> R.drawable.marker_blue
                        else -> when (roadData.severityLevel) {
                            "위험" -> R.drawable.marker_red
                            "경고" -> R.drawable.marker_orange
                            "안전" -> R.drawable.marker_green
                            else -> null
                        }
                    }

                    if (iconResId != null) {
                        marker.icon = getTransparentOverlay(iconResId)
                        marker.width = 100
                        marker.height = 100
                    } else {
                        // 정의되지 않은 경우 기존 회색 틴트 유지
                        marker.icon = MarkerIcons.BLACK
                        marker.iconTintColor = Color.GRAY
                    }
                    
                    marker.tag = "ROAD_${roadData.roadIdx}"

                    // 마커 클릭 이벤트 설정 - 침수인 경우에만 다이얼로그 표시
                    marker.setOnClickListener { overlay ->
                        // anomaly_type이 '침수'인 경우에만 다이얼로그 표시
                        if (roadData.anomalyType == "침수") {
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
                        }
                        
                        true // 이벤트 처리 완료
                    }

                    markers.add(marker)

                    Log.d("MainActivity", "[$index] 도로 마커 위치: 원본=(${roadData.latitude}, ${roadData.longitude}), 조정=(${marker.position.latitude}, ${marker.position.longitude}), 심각도: ${roadData.severityLevel}")

                } catch (e: Exception) {
                    Log.e("MainActivity", "[$index] 도로 마커 생성 실패: ${e.message}")
                }
            }

            Log.d("MainActivity", "도로 마커 생성 완료: ${markers.size}개")
            Log.d("MainActivity", "현재 통제 마커 수: ${controlMarkers.size}개 (변경되지 않아야 함)")

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
            Log.d("MainActivity", "=== 통제 Observer 시작 ===")
            Log.d("MainActivity", "현재 도로 마커 수: ${markers.size}")
            Log.d("MainActivity", "받은 도로 통제 리스트: ${roadControlList?.size}개")

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
                    // 도로 통제 마커 아이콘을 보라색 이미지로 교체 (배경 투명 처리)
                    marker.icon = getTransparentOverlay(R.drawable.marker_control)
                    marker.width = 120
                    marker.height = 120
                    
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

            // 마커 생성 완료 후 추가
            Log.d("MainActivity", "통제 마커 생성 완료: ${controlMarkers.size}개")
            Log.d("MainActivity", "현재 도로 마커 수: ${markers.size}개 (변경되지 않아야 함)")

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

    private fun checkMarkerStatus() {
        Log.d("MainActivity", "=== 마커 상태 확인 ===")
        Log.d("MainActivity", "도로 마커: ${markers.size}개")
        Log.d("MainActivity", "통제 마커: ${controlMarkers.size}개")

        val visibleRoadMarkers = markers.count { it.map != null }
        val visibleControlMarkers = controlMarkers.count { it.map != null }

        Log.d("MainActivity", "실제 표시된 도로 마커: ${visibleRoadMarkers}개")
        Log.d("MainActivity", "실제 표시된 통제 마커: ${visibleControlMarkers}개")
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        Log.d("MainActivity", "지도 준비 완료")
        naverMap = map

        // 잠시 기다린 후 지도 상태 확인
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d("MainActivity", "지도 준비 3초 후 상태 확인")
            checkMarkerStatus()
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

    // 위치 검색 실행
    private fun searchLocations(query: String): List<LocationData> {
        val results = mutableListOf<LocationData>()
        
        gwangjuLocations.forEach { location ->
            if (location.name.contains(query, ignoreCase = true)) {
                results.add(location)
            }
        }
        
        // 구별로 그룹화하여 정렬
        return results.sortedWith(compareBy({ it.type }, { it.parent ?: it.name }))
    }
    
    // 선택된 위치로 이동
    private fun moveToLocation(location: LocationData) {
        // 기존 위치 마커들 제거
        clearLocationMarkers()
        
        // 새로운 위치 마커 추가
        val marker = Marker()
        marker.position = LatLng(location.latitude, location.longitude)
        marker.map = naverMap
        
        // 구와 동에 따라 다른 아이콘 사용
        when (location.type) {
            "구" -> {
                marker.icon = MarkerIcons.BLUE
                marker.iconTintColor = Color.BLUE
                marker.width = 80
                marker.height = 80
            }
            "동" -> {
                marker.icon = MarkerIcons.GREEN
                marker.iconTintColor = Color.GREEN
                marker.width = 60
                marker.height = 60
            }
        }
        
        marker.tag = "LOCATION_${location.name}"
        
        // 마커 클릭 이벤트
        marker.setOnClickListener { _ ->
            val message = when (location.type) {
                "구" -> "🏛️ ${location.name}\n\n위치: ${location.latitude}, ${location.longitude}"
                "동" -> "🏘️ ${location.name}\n📍 소속: ${location.parent}\n\n위치: ${location.latitude}, ${location.longitude}"
                else -> "${location.name}\n\n위치: ${location.latitude}, ${location.longitude}"
            }
            
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
            true
        }
        
        locationMarkers.add(marker)
        
        // 카메라를 해당 위치로 이동
        val cameraPosition = CameraPosition(
            LatLng(location.latitude, location.longitude),
            15.0,
            0.0,
            0.0
        )
        naverMap.cameraPosition = cameraPosition
        
        android.widget.Toast.makeText(this, "${location.name}으로 이동했습니다", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    // 위치 마커들 제거
    private fun clearLocationMarkers() {
        locationMarkers.forEach { marker ->
            marker.map = null
        }
        locationMarkers.clear()
    }

    // 주어진 리소스에서 가장자리의 흰색(근사치) 배경만 투명 처리하고 내부 글씨의 흰색은 유지
    private fun getTransparentOverlay(resId: Int): OverlayImage {
        overlayImageCache[resId]?.let { return it }

        val original = BitmapFactory.decodeResource(resources, resId)
        val width = original.width
        val height = original.height
        val pixels = IntArray(width * height)
        original.getPixels(pixels, 0, width, 0, 0, width, height)

        // 허용 오차(흰색 근사치)와 flood fill로 테두리 배경만 투명화
        val tolerance = 20
        fun isNearWhite(color: Int): Boolean {
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            return r >= 255 - tolerance && g >= 255 - tolerance && b >= 255 - tolerance
        }

        val visited = BooleanArray(width * height)
        val queue: ArrayDeque<Int> = ArrayDeque()

        // 가장자리 픽셀 중 흰색(근사치)인 부분만 시드로 추가
        for (x in 0 until width) {
            val top = x
            val bottom = (height - 1) * width + x
            if (!visited[top] && isNearWhite(pixels[top])) { visited[top] = true; queue.add(top) }
            if (!visited[bottom] && isNearWhite(pixels[bottom])) { visited[bottom] = true; queue.add(bottom) }
        }
        for (y in 0 until height) {
            val left = y * width
            val right = y * width + (width - 1)
            if (!visited[left] && isNearWhite(pixels[left])) { visited[left] = true; queue.add(left) }
            if (!visited[right] && isNearWhite(pixels[right])) { visited[right] = true; queue.add(right) }
        }

        // BFS로 연결된 흰 배경만 투명화 (내부 흰 글씨는 보존)
        while (queue.isNotEmpty()) {
            val idx = queue.removeFirst()
            pixels[idx] = (pixels[idx] and 0x00FFFFFF)
            val x = idx % width
            val y = idx / width

            // 좌
            if (x > 0) {
                val n = idx - 1
                if (!visited[n] && isNearWhite(pixels[n])) { visited[n] = true; queue.add(n) }
            }
            // 우
            if (x + 1 < width) {
                val n = idx + 1
                if (!visited[n] && isNearWhite(pixels[n])) { visited[n] = true; queue.add(n) }
            }
            // 상
            if (y > 0) {
                val n = idx - width
                if (!visited[n] && isNearWhite(pixels[n])) { visited[n] = true; queue.add(n) }
            }
            // 하
            if (y + 1 < height) {
                val n = idx + width
                if (!visited[n] && isNearWhite(pixels[n])) { visited[n] = true; queue.add(n) }
            }
        }

        // 내부의 거의-흰색 픽셀을 순백(불투명)으로 보정해 가독성 향상
        val whitenTolerance = 35
        fun isNearWhiteTol(color: Int, tol: Int): Boolean {
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            return r >= 255 - tol && g >= 255 - tol && b >= 255 - tol
        }
        for (i in pixels.indices) {
            if (!visited[i] && isNearWhiteTol(pixels[i], whitenTolerance)) {
                pixels[i] = (0xFF shl 24) or 0x00FFFFFF
            }
        }

        val processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        processed.setPixels(pixels, 0, width, 0, 0, width, height)

        val overlay = OverlayImage.fromBitmap(processed)
        overlayImageCache[resId] = overlay
        return overlay
    }
}