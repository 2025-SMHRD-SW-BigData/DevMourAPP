package com.example.devmour

import android.content.Intent
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.ArrayDeque
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
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
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.MapFragment
import com.example.devmour.viewmodel.RoadViewModel
import com.example.devmour.viewmodel.RoadControlViewModel

import com.example.devmour.data.LocationData
import com.example.devmour.alert.MainActivityAlert
import com.example.devmour.auth.LoginManager
import com.example.devmour.auth.SessionManager
import com.example.devmour.data.AddressSearchResult
import com.example.devmour.viewmodel.GeocodingViewModel
import com.example.devmour.ReportActivity

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var geocodingViewModel: GeocodingViewModel

    private var LOCATION_PERMISSION = 1004
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var roadViewModel: RoadViewModel
    private lateinit var roadControlViewModel: RoadControlViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    
    // 실시간 위치 표시를 위한 변수들
    private var currentLocationMarker: Marker? = null
    private var locationUpdateHandler: android.os.Handler? = null
    private val LOCATION_UPDATE_INTERVAL = 3000L // 3초마다 위치 업데이트
    
    // 위험 구역 감지를 위한 변수들
    private var isInDangerZone = false
    private var dangerAnimationHandler: android.os.Handler? = null
    private val DANGER_ANIMATION_INTERVAL = 15L // 위험 시 15ms마다 애니메이션 (숫자낮을수록빠름)
    private val NORMAL_ANIMATION_INTERVAL = 50L // 일반 시 50ms마다 애니메이션
    
    // 파동 애니메이션을 위한 변수들
    private val rippleOverlays = mutableListOf<CircleOverlay>()
    private var rippleAnimationHandler: android.os.Handler? = null
    private val RIPPLE_ANIMATION_INTERVAL = 30L // 60fps 애니메이션
    private val MAX_RIPPLE_COUNT = 3 // 동시에 표시될 파동 개수


    // 네비게이션 바 요소들
    private lateinit var btnNotification: android.view.View
    private lateinit var btnMain: android.view.View
    private lateinit var btnReport: android.view.View
    
    // 네비게이션 바 아이콘들
    private lateinit var ivNotification: android.widget.ImageView
    private lateinit var ivMain: android.widget.ImageView
    private lateinit var ivReport: android.widget.ImageView

    // 마커 리스트를 저장할 변수
    private val markers = mutableListOf<Marker>()
    private val controlMarkers = mutableListOf<Marker>()
    private val floodMarkers = mutableListOf<Marker>() // 침수 마커
    private val locationMarkers = mutableListOf<Marker>() // 위치 검색 마커
    private val overlayImageCache = mutableMapOf<Int, OverlayImage>()


    // 안전마커 토글 상태 관리
    private var isSafeMarkersVisible = true

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
        // 키보드 설정 - 완전한 제어
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        // ViewModel 초기화
        roadViewModel = ViewModelProvider(this)[RoadViewModel::class.java]
        roadControlViewModel = ViewModelProvider(this)[RoadControlViewModel::class.java]
        Log.d("MainActivity", "ViewModel 초기화 완료")

    // 기존 ViewModel 초기화 다음에 추가
        geocodingViewModel = ViewModelProvider(this)[GeocodingViewModel::class.java]

        // UI 초기화
        initNavigationBar()

        // 데이터 관찰 설정
        observeRoads()
        observeRoadControls()
        observeFloodData()
        observeGeocoding()


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
        
        // 네비게이션 바 아이콘들 초기화
        ivNotification = findViewById(R.id.ivNotification)
        ivMain = findViewById(R.id.ivMain)
        ivReport = findViewById(R.id.ivReport)


        // GPS 위치 이동 버튼 초기화
        val btnGpsLocation = findViewById<android.widget.ImageButton>(R.id.btn_gps_location)
        
        // 안전마커 토글 버튼 초기화
        val btnToggleSafeMarkers = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_toggle_safe_markers)

        // 알림 버튼 클릭
        btnNotification.setOnClickListener {
            // MainActivityAlert 로 이동
            val intent = android.content.Intent(this, MainActivityAlert::class.java)
            startActivity(intent)
        }
        
        // 메인화면 버튼 클릭 (현재 화면이므로 아무 동작 안함)
        btnMain.setOnClickListener {
            // 현재 메인화면이므로 아무 동작 안함
        }
        
        // 민원접수 버튼 클릭
        btnReport.setOnClickListener {
            try {
                android.util.Log.d("MainActivity", "민원제보 버튼 클릭됨")

                // 로그인 상태 확인
                if (LoginManager.isLoggedIn(this)) {
                    android.util.Log.d("MainActivity", "이미 로그인됨 - ReportActivity로 이동")
                    val intent = android.content.Intent(this, ReportActivity::class.java)
                    startActivity(intent)
                } else {
                    android.util.Log.d("MainActivity", "로그인 필요 - LoginActivity로 이동")
                    val intent = android.content.Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "민원제보 버튼 클릭 오류: ${e.message}", e)
                android.widget.Toast.makeText(this, "오류가 발생했습니다: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // GPS 위치 이동 버튼 클릭
        btnGpsLocation.setOnClickListener {
            moveToCurrentLocation()
        }
        
        // 안전마커 토글 버튼 클릭
        btnToggleSafeMarkers.setOnClickListener {
            toggleSafeMarkers()
            updateSafeMarkerToggleButton(btnToggleSafeMarkers)
        }

        // 초기 버튼 상태 설정
        updateSafeMarkerToggleButton(btnToggleSafeMarkers)

        // 현재 메인화면이므로 메인화면 아이콘 텍스트 색상을 강조
        (btnMain as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#2f354f"))
            }
        }
        // 현재 메인화면이므로 메인화면 아이콘과 텍스트 색상을 강조
        setNavigationBarState("main")

        // 기존 검색 버튼을 위치 검색 기능과 연결
        val searchButton = findViewById<android.widget.Button>(R.id.btnSearch)
        val searchEditText = findViewById<android.widget.EditText>(R.id.etSearch)
        
        // EditText 클릭 시 키보드 강제 표시 및 커스텀 메뉴 차단
        searchEditText?.setOnClickListener {
            searchEditText.requestFocus()
            // 즉시 키보드 표시 시도
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
            
            // 추가 시도들
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
            }, 50)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
            }, 100)
        }
        
        // EditText 포커스 변경 시 키보드 강제 표시
        searchEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 즉시 키보드 표시
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
                
                // 추가로 토글 시도
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    imm.toggleSoftInput(android.view.inputmethod.InputMethodManager.SHOW_FORCED, 0)
                }, 200)
            }
        }
        
        searchButton?.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                // API 우선 검색 시작
                geocodingViewModel.searchAddress(searchQuery)
//                val searchResults = searchLocations(searchQuery)
//                if (searchResults.isEmpty()) {
//                    // 첫 번째 검색 결과로 이동
//                    moveToLocation(searchResults.first())
//                    android.widget.Toast.makeText(this, "${searchResults.first().name}으로 이동했습니다", android.widget.Toast.LENGTH_SHORT).show()
//
                    // 검색 완료 후 키보드 숨기기
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                    searchEditText.clearFocus()

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

        // 메인화면이므로 메인 아이콘 활성화
        setNavigationBarState("main")
    }
    
    // 네비게이션 바 상태 설정 함수
    private fun setNavigationBarState(currentPage: String) {
        // 모든 아이콘을 기본 상태(흰색)로 초기화
        ivNotification.setImageResource(R.drawable.alarm_w)
        ivMain.setImageResource(R.drawable.main_w)
        ivReport.setImageResource(R.drawable.report_w)

        // 모든 텍스트를 기본 색상으로 초기화
        (btnNotification as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }
        (btnMain as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }
        (btnReport as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
            if (textView is android.widget.TextView) {
                textView.setTextColor(Color.parseColor("#666666"))
            }
        }

        // 현재 페이지에 따라 아이콘과 텍스트 색상 설정
        when (currentPage) {
            "notification" -> {
                ivNotification.setImageResource(R.drawable.alarm_b)
                (btnNotification as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
            "main" -> {
                ivMain.setImageResource(R.drawable.main_b)
                (btnMain as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
            "report" -> {
                ivReport.setImageResource(R.drawable.report_b)
                (btnReport as android.widget.LinearLayout).getChildAt(1)?.let { textView ->
                    if (textView is android.widget.TextView) {
                        textView.setTextColor(Color.parseColor("#2f354f"))
                    }
                }
            }
        }
    }

    // total_score를 기준으로 마커 아이콘을 결정하는 함수
    private fun getMarkerIconByScore(totalScore: Double): Int {
        return when {
            totalScore >= 0.0 && totalScore <= 4.0 -> R.drawable.marker_green    // 안전 등급
            totalScore >= 4.1 && totalScore <= 7.0 -> R.drawable.marker_orange   // 경고 등급
            totalScore >= 7.1 && totalScore <= 10.0 -> R.drawable.marker_red     // 위험 등급
            else -> R.drawable.marker_green  // 기본값
        }
    }

    // total_score를 기준으로 등급을 결정하는 함수
    private fun getGradeByScore(totalScore: Double): String {
        return when {
            totalScore >= 0.0 && totalScore <= 4.0 -> "안전"
            totalScore >= 4.1 && totalScore <= 7.0 -> "경고"
            totalScore >= 7.1 && totalScore <= 10.0 -> "위험"
            else -> "안전"
        }
    }

    private fun observeRoads() {
        Log.d("MainActivity", "=== Observer 설정 시작 ===")

        roadViewModel.roads.observe(this) { roadList ->
            Log.d("MainActivity", "=== 도로 Observer 시작 ===")

            // null 체크 및 초기화 확인
            if (roadList == null || !::naverMap.isInitialized) {
                Log.w("MainActivity", "조건 미충족 - 처리중단")
                return@observe
            }

            // 도로 리스트가 비어있는지 확인
            if (roadList.isEmpty()) {
                Log.w("MainActivity", "받은 도로 리스트가 비어있음!")
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

                    // total_score를 기준으로 마커 아이콘 결정
                    val iconResId = getMarkerIconByScore(roadData.totalScore)
                    
                    try {
                        marker.icon = getTransparentOverlay(iconResId)
                        marker.width = 150
                        marker.height = 150


                        // 수정된 코드
                        marker.minZoom = 1.0   // 모든 줌 레벨에서 표시
                        marker.maxZoom = 21.0
                        marker.isHideCollidedMarkers = false
                        marker.isHideCollidedSymbols = false
                        marker.isForceShowIcon = true
                        marker.isIconPerspectiveEnabled = false  // 3D 효과 비활성화로 안정성 향상

// 마커별 고유 zIndex 설정 (겹침 방지)
                        marker.zIndex = when {
                            roadData.totalScore >= 7.1 -> 1000 + index  // 위험 마커 최우선
                            roadData.totalScore >= 4.1 -> 500 + index   // 경고 마커
                            else -> 100 + index  // 안전 마커
                        }

                    } catch (e: Exception) {
                        Log.e("MainActivity", "마커 아이콘 설정 실패: ${e.message}")
                        // 실패 시 기본 아이콘 사용
                        marker.icon = MarkerIcons.BLACK
                        marker.iconTintColor = Color.GRAY
                    }

                    // 마커를 리스트에 추가
                    markers.add(marker)
                    
                    Log.d("MainActivity", "마커 추가 완료: ${index + 1}/${roadList.size} - 위치: ${roadData.latitude}, ${roadData.longitude}, 점수: ${roadData.totalScore}, 등급: ${getGradeByScore(roadData.totalScore)}")

                } catch (e: Exception) {
                    Log.e("MainActivity", "마커 생성 실패 (인덱스: $index): ${e.message}")
                }
            }

            Log.d("MainActivity", "=== 도로 마커 생성 완료 ===")
            Log.d("MainActivity", "생성된 마커 수: ${markers.size}")
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

            // null 체크 및 초기화 확인
            if (roadControlList == null || !::naverMap.isInitialized) {
                Log.w("MainActivity", "도로 통제 리스트가 null임 - 처리중단")
                return@observe
            }



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
                    marker.width = 150
                    marker.height = 150

                    // 다음 코드들 추가
                    marker.minZoom = 1.0
                    marker.maxZoom = 21.0
                    marker.isHideCollidedMarkers = false
                    marker.isHideCollidedSymbols = false
                    marker.isForceShowIcon = true
                    marker.isIconPerspectiveEnabled = false
                    marker.zIndex = 2000 + index  // 통제 마커는 가장 높은 우선순위

                    marker.tag = "CONTROL_${roadControlData.controlIdx}"

                    // 마커 클릭 이벤트 설정 - 다이얼로그 표시
                    marker.setOnClickListener { overlay ->
                        val message =
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

    private fun observeFloodData() {
        Log.d("MainActivity", "=== 침수 Observer 설정 시작 ===")

        roadControlViewModel.floodData.observe(this) { floodList ->
            Log.d("MainActivity", "=== 침수 Observer 시작 ===")
            Log.d("MainActivity", "현재 침수 마커 수: ${floodMarkers.size}")
            Log.d("MainActivity", "받은 침수 리스트: ${floodList?.size}개")

            // null 체크 naverMap 초기화 확인
            if (floodList == null || !::naverMap.isInitialized ) {
                Log.w("MainActivity", "침수 리스트가 null임 - 처리중단")
                return@observe
            }



            Log.d("MainActivity", "모든 조건 통과 - 침수 마커 처리 시작")

            // 기존 침수 마커들 제거
            Log.d("MainActivity", "기존 침수 마커 제거: ${floodMarkers.size}개")
            floodMarkers.forEach { marker ->
                marker.map = null  // 지도에서 마커 제거
            }
            floodMarkers.clear()
            Log.d("MainActivity", "기존 침수 마커들 제거 완료")

            // 침수 리스트가 비어있는지 확인
            if (floodList.isEmpty()) {
                Log.w("MainActivity", "받은 침수 리스트가 비어있음!")
                return@observe
            }

            // 새로운 침수 마커들 추가
            floodList.forEachIndexed { index, floodData ->
                try {
                    val marker = Marker()

                    // 겹치지 않도록 약간씩 위치 조정
                    val offsetLat = (index % 5) * 0.0001  // 0.0001도씩 차이
                    val offsetLng = (index / 5) * 0.0001

                    marker.position = LatLng(
                        floodData.latitude + offsetLat,
                        floodData.longitude + offsetLng
                    )
                    marker.map = naverMap

                    // 침수 마커 아이콘 설정
                    marker.icon = getTransparentOverlay(R.drawable.marker_blue)
                    marker.width = 150
                    marker.height = 150
                    
                    // 침수 마커가 공사통제 마커보다 앞에 표시되도록 zIndex 설정
                    marker.zIndex = 3000 + index  // 침수 마커는 가장 높은 우선순위

                    marker.tag = "FLOOD_${floodData.controlIdx}"

                    // 침수 마커 클릭 이벤트 설정 - 다이얼로그 표시
                    marker.setOnClickListener { overlay ->
                        val message =
                                "📝 설명: ${floodData.controlDesc}\n" +
                                "🕐 시작: ${floodData.controlStTm}\n" +
                                "🕐 종료: ${floodData.controlEdTm ?: "미정"}\n" +
                                "📍 주소: ${floodData.controlAddr}\n" +
                                "🌊 통제 유형: ${floodData.controlType}\n" +
                                "✅ 완료 여부: ${if (floodData.completed == "Y") "완료" else "진행중"}"
                        
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("🌊 침수 위험 구역 정보")
                            .setMessage(message)
                            .setPositiveButton("확인") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                        
                        alertDialog.show()
                        
                        // 제목 텍스트 색상 설정 (파란색)
                        val titleTextView = alertDialog.findViewById<android.widget.TextView>(android.R.id.title)
                        titleTextView?.setTextColor(Color.rgb(0, 123, 255))
                        
                        true // 이벤트 처리 완료
                    }

                    floodMarkers.add(marker)

                    Log.d("MainActivity", "[$index] 침수 마커 위치: 원본=(${floodData.latitude}, ${floodData.longitude}), 조정=(${marker.position.latitude}, ${marker.position.longitude})")

                } catch (e: Exception) {
                    Log.e("MainActivity", "[$index] 침수 마커 생성 실패: ${e.message}")
                }
            }

            // 마커 생성 완료 후 추가
            Log.d("MainActivity", "침수 마커 생성 완료: ${floodMarkers.size}개")
            Log.d("MainActivity", "현재 도로 마커 수: ${markers.size}개 (변경되지 않아야 함)")
            Log.d("MainActivity", "현재 통제 마커 수: ${controlMarkers.size}개 (변경되지 않아야 함)")

            // 실제로 지도에 표시된 마커 개수 확인
            val visibleFloodMarkers = floodMarkers.count { it.map != null }
            Log.d("MainActivity", "지도에 실제 표시된 침수 마커 수: ${visibleFloodMarkers}")

            Log.d("MainActivity", "최종: ${floodMarkers.size}개 침수 마커 추가 완료")
        }
    }

    private fun observeGeocoding() {
        Log.d("MainActivity", "=== Geocoding Observer 설정 시작 ===")

        geocodingViewModel.searchResult.observe(this) { searchResult ->
            searchResult?.let { result ->
                Log.d("MainActivity", "주소 검색 결과: ${result.address} -> ${result.latitude}, ${result.longitude}")

                // 검색된 위치로 이동
                moveToSearchedLocation(result)

                android.widget.Toast.makeText(this,
                    "${result.address}로 이동했습니다",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        geocodingViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.e("MainActivity", "API 주소 검색 실패: $it")

                // API 검색 실패 시 로컬 검색으로 fallback
                performLocalSearch()
            }
        }

        geocodingViewModel.isLoading.observe(this) { isLoading ->
            Log.d("MainActivity", "주소 검색 로딩 상태: $isLoading")
            // 로딩 인디케이터 표시/숨김 처리 가능
        }
    }
    // 로컬 검색을 수행하는 별도 함수 추가
    private fun performLocalSearch() {
        val searchEditText = findViewById<android.widget.EditText>(R.id.etSearch)
        val searchQuery = searchEditText?.text.toString().trim()

        if (searchQuery.isNotEmpty()) {
            val localResults = searchLocations(searchQuery)
            if (localResults.isNotEmpty()) {
                // 로컬 검색 결과가 있으면 첫 번째 결과로 이동
                moveToLocation(localResults.first())
                android.widget.Toast.makeText(this,
                    "로컬 검색: ${localResults.first().name}으로 이동했습니다",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                // 로컬 검색 결과도 없으면 에러 메시지
                android.widget.Toast.makeText(this,
                    "검색 결과가 없습니다",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun moveToSearchedLocation(searchResult: AddressSearchResult) {
        // 기존 위치 마커들 제거
        clearLocationMarkers()

        // 새로운 위치 마커 추가
        val marker = com.naver.maps.map.overlay.Marker()
        marker.position = com.naver.maps.geometry.LatLng(searchResult.latitude, searchResult.longitude)
        marker.map = naverMap

        // 검색된 주소 마커 스타일 설정
        marker.icon = com.naver.maps.map.util.MarkerIcons.RED
        marker.iconTintColor = android.graphics.Color.RED
        marker.width = 100
        marker.height = 100
        marker.captionText = searchResult.address

        marker.tag = "SEARCHED_LOCATION"

        // 마커 클릭 이벤트
        marker.setOnClickListener { _ ->
            val message = "검색된 주소\n${searchResult.address}\n\n위치: ${searchResult.latitude}, ${searchResult.longitude}"
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
            true
        }

        locationMarkers.add(marker)

        // 카메라를 해당 위치로 이동
        val cameraPosition = com.naver.maps.map.CameraPosition(
            com.naver.maps.geometry.LatLng(searchResult.latitude, searchResult.longitude),
            16.0,
            0.0,
            0.0
        )
        naverMap.cameraPosition = cameraPosition
    }

    private fun checkMarkerStatus() {
        Log.d("MainActivity", "=== 마커 상태 확인 ===")
        Log.d("MainActivity", "도로 마커: ${markers.size}개")
        Log.d("MainActivity", "통제 마커: ${controlMarkers.size}개")
        Log.d("MainActivity", "침수 마커: ${floodMarkers.size}개")

        val visibleRoadMarkers = markers.count { it.map != null }
        val visibleControlMarkers = controlMarkers.count { it.map != null }
        val visibleFloodMarkers = floodMarkers.count { it.map != null }

        Log.d("MainActivity", "실제 표시된 도로 마커: ${visibleRoadMarkers}개")
        Log.d("MainActivity", "실제 표시된 통제 마커: ${visibleControlMarkers}개")
        Log.d("MainActivity", "실제 표시된 침수 마커: ${visibleFloodMarkers}개")
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        Log.d("MainActivity", "지도 준비 완료")
        naverMap = map

// 기존 onMapReady 함수 내부에 추가
        naverMap.uiSettings.isZoomControlEnabled = true

// 마커 렌더링 최적화 설정 추가
        naverMap.setLayerGroupEnabled(com.naver.maps.map.NaverMap.LAYER_GROUP_BUILDING, true)
        naverMap.mapType = com.naver.maps.map.NaverMap.MapType.Basic

// 마커 클러스터링 비활성화 (모든 마커 개별 표시)
        naverMap.uiSettings.isCompassEnabled = false

        // 잠시 기다린 후 지도 상태 확인
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d("MainActivity", "지도 준비 3초 후 상태 확인")
            checkMarkerStatus()
        }, 3000)

        // 카메라 설정 (테스트 위치로 설정)
//        val testLatitude = 35.1488
//        val testLongitude = 126.9154
//        val cameraPosition = CameraPosition(
//            LatLng(testLatitude, testLongitude),
//            16.0,
//            20.0,
//            0.0
//        )
//        naverMap.cameraPosition = cameraPosition
//        Log.d("MainActivity", "테스트 위치로 카메라 설정 완료: $testLatitude, $testLongitude")
//
        // 지도 클릭 이벤트 비활성화 (마커 추가/삭제 기능 제거)
        naverMap.setOnMapClickListener { point, coord ->
            Log.d("MainActivity", "지도 클릭: ${coord.latitude}, ${coord.longitude}")
        }
        
        naverMap.locationSource = locationSource
        
        // 위치 추적 설정 (테스트 모드)
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isLocationButtonEnabled = false
        
        // 줌 컨트롤 활성화
        naverMap.uiSettings.isZoomControlEnabled = true
        
        // 테스트 모드에서는 위치 권한 요청하지 않음
        ActivityCompat.requestPermissions(this, PERMISSION, LOCATION_PERMISSION)
        
        // 지도가 준비되면 도로 데이터 로드
        Log.d("MainActivity", "도로 데이터 로드 시작")
        roadViewModel.loadRoads()
        
        // 도로 통제 데이터 로드
        Log.d("MainActivity", "도로 통제 데이터 로드 시작")
        roadControlViewModel.loadRoadControls()
        
        // 침수 데이터 로드
        Log.d("MainActivity", "침수 데이터 로드 시작")
        roadControlViewModel.loadFloodData()
        
        // 테스트 위치 마커 즉시 표시
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startLocationUpdates()
        }, 1000) // 1초 후 시작

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
                    stopLocationUpdates()
                    android.widget.Toast.makeText(this, "위치 권한이 필요합니다", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    startLocationUpdates()
//                    android.widget.Toast.makeText(this, "실시간 위치 추적이 시작되었습니다", android.widget.Toast.LENGTH_SHORT).show()
                }
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
                marker.width = 140
                marker.height = 140
            }
            "동" -> {
                marker.icon = MarkerIcons.GREEN
                marker.iconTintColor = Color.GREEN
                marker.width = 120
                marker.height = 120
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
        val toast = Toast.makeText(this, "${location.name}으로 이동했습니다", android.widget.Toast.LENGTH_SHORT)

        toast.setGravity(Gravity.TOP, 0,100);
        toast.show()
    }

    // 위치 마커들 제거
    private fun clearLocationMarkers() {
        locationMarkers.forEach { marker ->
            marker.map = null
        }
        locationMarkers.clear()
    }
    
    // 현재 위치로 이동
    private fun moveToCurrentLocation() {
        if (::naverMap.isInitialized) {
            // 현재 위치 마커가 있다면 해당 위치로 이동
            currentLocationMarker?.let { marker ->
                val cameraPosition = CameraPosition(
                    marker.position,
                    16.0,
                    0.0,
                    0.0
                )
                naverMap.cameraPosition = cameraPosition
                android.widget.Toast.makeText(this, "현재 위치로 이동했습니다", android.widget.Toast.LENGTH_SHORT).show()
            } ?: run {
                // 현재 위치 마커가 없다면 기본 위치(광주시 중심)로 이동
                val defaultLocation = LatLng(35.1488, 126.9154)
                val cameraPosition = CameraPosition(
                    defaultLocation,
                    16.0,
                    0.0,
                    0.0
                )
                naverMap.cameraPosition = cameraPosition
                android.widget.Toast.makeText(this, "기본 위치로 이동했습니다", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 실시간 위치 업데이트 시작
    private fun startLocationUpdates() {
        Log.d("MainActivity", "startLocationUpdates 호출됨")
        
        if (locationUpdateHandler == null) {
            locationUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
        }
        
        // 즉시 첫 번째 위치 업데이트 실행
        updateCurrentLocation()
        
        val locationRunnable = object : Runnable {
            override fun run() {
                updateCurrentLocation()
                locationUpdateHandler?.postDelayed(this, LOCATION_UPDATE_INTERVAL)
            }
        }
        
        locationUpdateHandler?.postDelayed(locationRunnable, LOCATION_UPDATE_INTERVAL)
        Log.d("MainActivity", "실시간 위치 업데이트 시작")
    }
    
    // 실시간 위치 업데이트 중지
    private fun stopLocationUpdates() {
        locationUpdateHandler?.removeCallbacksAndMessages(null)
        locationUpdateHandler = null
        
        // FusedLocationProviderClient의 위치 업데이트도 중지
        try {
            fusedLocationClient.removeLocationUpdates(object : com.google.android.gms.location.LocationCallback() {})
        } catch (e: Exception) {
            Log.e("MainActivity", "위치 업데이트 제거 실패: ${e.message}")
        }
        
        Log.d("MainActivity", "실시간 위치 업데이트 중지")
    }
    
    // 현재 위치 업데이트
    private fun updateCurrentLocation() {
        // 테스트용 하드코딩된 위치 (일시적)
       //광주역 좌표
//        val testLatitude = 35.165
//      val testLongitude = 126.909

        //금남로4가 좌표
//        val testLatitude = 35.1488
//        val testLongitude = 126.9154
      //임동오거리
//        val testLatitude =  35.159588
//        val testLongitude = 126.899809
//
//        // 하드코딩된 위치로 마커 업데이트
//        updateLocationMarker(LatLng(testLatitude, testLongitude))
//        Log.d("MainActivity", "테스트 위치로 업데이트: $testLatitude, $testLongitude")
//
        // 실제 GPS 위치 대신 테스트 위치 사용

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // 마지막 알려진 위치 가져오기
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    updateLocationMarker(LatLng(it.latitude, it.longitude))
                    Log.d("MainActivity", "위치 업데이트: ${it.latitude}, ${it.longitude}")
                }
            }

            // 실시간 위치 업데이트 요청 (더 정확한 위치)
            try {
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(10000) // 10초마다
                    .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                    .build()

                fusedLocationClient.requestLocationUpdates(locationRequest,
                    object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            locationResult.lastLocation?.let { location ->
                                updateLocationMarker(LatLng(location.latitude, location.longitude))
                                Log.d("MainActivity", "실시간 위치 업데이트: ${location.latitude}, ${location.longitude}")
                            }
                        }
                    }, android.os.Looper.getMainLooper())
            } catch (e: Exception) {
                Log.e("MainActivity", "실시간 위치 업데이트 요청 실패: ${e.message}")
            }
        }

    }
    
    // 위치 마커 업데이트
    private fun updateLocationMarker(position: LatLng) {
        Log.d("MainActivity", "updateLocationMarker 호출됨: $position")
        
        // 위험 구역 확인
        val wasInDangerZone = isInDangerZone
        isInDangerZone = checkDangerZone(position)
        
        if (isInDangerZone != wasInDangerZone) {
            Log.d("MainActivity", "위험 구역 상태 변경: $isInDangerZone")
        }
        
                 if (currentLocationMarker == null) {
             // 현재 위치 마커 생성 및 표시
             currentLocationMarker = Marker().apply {
                 this.position = position
                 this.map = naverMap
                 this.tag = "CURRENT_LOCATION"


                 // 추가할 설정들
                 this.minZoom = 1.0
                 this.maxZoom = 21.0
                 this.isHideCollidedMarkers = false
                 this.isHideCollidedSymbols = false
                 this.isForceShowIcon = true
                 this.isIconPerspectiveEnabled = false
                 this.zIndex = 3000  // 현재 위치는 최우선


                 // 현재 위치 마커를 항상 숨김 (파동 효과만 표시)
                 this.map = null
                 Log.d("MainActivity", "현재 위치 마커: 숨김 - 파동 효과만 표시")
             }
            
            Log.d("MainActivity", "위치 추적 시작: ${position}")
            
            // 파동 애니메이션 시작
            startPulsingAnimation()
        } else {
            // 기존 마커 위치 업데이트
            currentLocationMarker?.position = position
            
                         // 현재 위치 마커는 항상 숨김 (파동 효과만 표시)
                 currentLocationMarker?.let { marker ->
                     marker.map = null
                     Log.d("MainActivity", "현재 위치 마커: 항상 숨김 - 파동 효과만 표시")
                 }
                 
                 startPulsingAnimation()
            
            Log.d("MainActivity", "위치 업데이트됨: $position")
        }
    }
    
    // 위험 구역 감지 함수 (total_score 7.1~10.0 범위의 마커와의 거리 계산)
    private fun checkDangerZone(currentPosition: LatLng): Boolean {
        val dangerRadius = 300.0 // 300미터 반경
        
        // 도로 마커들 중 위험한 마커 확인 (total_score 7.1~10.0)
        roadViewModel.roads.value?.forEach { roadData ->
            if (roadData.totalScore >= 7.1 && roadData.totalScore <= 10.0) {
                val distance = calculateDistance(currentPosition, LatLng(roadData.latitude, roadData.longitude))
                if (distance <= dangerRadius) {
                    Log.d("MainActivity", "위험 마커 감지됨: 거리 ${distance}m, total_score: ${roadData.totalScore}")
                    return true
                }
            }
        }
        
        return false
    }
    
    // 두 지점 간의 거리 계산 (미터 단위)
    private fun calculateDistance(pos1: LatLng, pos2: LatLng): Double {
        val lat1 = Math.toRadians(pos1.latitude)
        val lat2 = Math.toRadians(pos2.latitude)
        val deltaLat = Math.toRadians(pos2.latitude - pos1.latitude)
        val deltaLng = Math.toRadians(pos2.longitude - pos1.longitude)
        
        val a = kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
                kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
                kotlin.math.sin(deltaLng / 2) * kotlin.math.sin(deltaLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return 6371000 * c // 지구 반지름 * c (미터 단위)
    }
    

    
    // 파동 애니메이션 시작
    private fun startPulsingAnimation() {
        currentLocationMarker?.let { marker ->
            // 기존 애니메이션 정리
            dangerAnimationHandler?.removeCallbacksAndMessages(null)
            rippleAnimationHandler?.removeCallbacksAndMessages(null)
            
            // 기존 파동 오버레이들 제거
            clearRippleOverlays()
            
            // 파동 애니메이션 시작
            startRippleAnimation()
        }
    }
    
    // 파동 오버레이들 제거
    private fun clearRippleOverlays() {
        rippleOverlays.forEach { overlay ->
            overlay.map = null
        }
        rippleOverlays.clear()
    }
    
    // 파동 애니메이션 시작
    private fun startRippleAnimation() {
        currentLocationMarker?.let { marker ->
            if (rippleAnimationHandler == null) {
                rippleAnimationHandler = android.os.Handler(android.os.Looper.getMainLooper())
            }
            
            var animationStep = 0
            val rippleRunnable = object : Runnable {
                override fun run() {
                    // 새로운 파동 생성 (최대 개수 제한)
                    if (rippleOverlays.size < MAX_RIPPLE_COUNT) {
                        createRippleOverlay(marker.position)
                    }
                    
                    // 기존 파동들 애니메이션 업데이트
                    updateRippleAnimation()
                    
                    // 완성된 파동들 제거
                    removeCompletedRipples()
                    
                    animationStep++
                    
                    // 위험 구역 여부에 따라 애니메이션 속도 조절
                    val interval = if (isInDangerZone) RIPPLE_ANIMATION_INTERVAL else RIPPLE_ANIMATION_INTERVAL * 2
                    rippleAnimationHandler?.postDelayed(this, interval)
                }
            }
            
            rippleAnimationHandler?.post(rippleRunnable)
        }
    }
    
    // 새로운 파동 오버레이 생성
    private fun createRippleOverlay(position: LatLng) {
        val ripple = CircleOverlay().apply {
            this.center = position
            this.radius = 5.0 // 시작 반지름 (미터)
            this.color = if (isInDangerZone) android.graphics.Color.argb(100, 255, 0, 0) else android.graphics.Color.argb(100, 0, 123, 255)
            this.outlineColor = if (isInDangerZone) android.graphics.Color.argb(150, 255, 0, 0) else android.graphics.Color.argb(150, 0, 123, 255)
            this.outlineWidth = 2
            this.map = naverMap
            this.tag = "RIPPLE_${System.currentTimeMillis()}"
        }
        
        rippleOverlays.add(ripple)
    }
    
    // 파동 애니메이션 업데이트
    private fun updateRippleAnimation() {
        rippleOverlays.forEach { ripple ->
            // 반지름 증가 (파동이 퍼져나가는 효과)
            val currentRadius = ripple.radius
            val newRadius = currentRadius + (if (isInDangerZone) 2.0 else 1.5) // 위험 시 더 빠르게
            
            // 투명도 감소 (멀어질수록 흐려지는 효과)
            val currentAlpha = (ripple.color shr 24) and 0xFF
            val newAlpha = (currentAlpha - 3).coerceAtLeast(0)
            
            ripple.radius = newRadius
            ripple.color = android.graphics.Color.argb(newAlpha, 
                (ripple.color shr 16) and 0xFF,
                (ripple.color shr 8) and 0xFF,
                ripple.color and 0xFF
            )
            ripple.outlineColor = android.graphics.Color.argb((newAlpha * 1.5).toInt().coerceAtMost(255), 
                (ripple.outlineColor shr 16) and 0xFF,
                (ripple.outlineColor shr 8) and 0xFF,
                ripple.outlineColor and 0xFF
            )
        }
    }
    
    // 완성된 파동들 제거
    private fun removeCompletedRipples() {
        val completedRipples = rippleOverlays.filter { ripple ->
            val alpha = (ripple.color shr 24) and 0xFF
            alpha <= 0 || ripple.radius > 100.0 // 투명해지거나 너무 커진 파동 제거
        }
        
        completedRipples.forEach { ripple ->
            ripple.map = null
            rippleOverlays.remove(ripple)
        }
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
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        
        // 위험 애니메이션 핸들러 정리
        dangerAnimationHandler?.removeCallbacksAndMessages(null)
        dangerAnimationHandler = null
        
        // 파동 애니메이션 핸들러 정리
        rippleAnimationHandler?.removeCallbacksAndMessages(null)
        rippleAnimationHandler = null
        
        // 파동 오버레이들 정리
        clearRippleOverlays()
        
        // 침수 마커들 정리
        floodMarkers.forEach { marker ->
            marker.map = null
        }
        floodMarkers.clear()
        
        currentLocationMarker?.map = null
        currentLocationMarker = null

        // 앱이 정상 종료될 때 정리 작업 수행
        SessionManager.markCleanExit(this, true)
    }

    // 안전마커 토글 기능
    private fun toggleSafeMarkers() {
        isSafeMarkersVisible = !isSafeMarkersVisible
        updateSafeMarkersVisibility()

//        val message = if (isSafeMarkersVisible) "안전마커가 표시됩니다" else "안전마커가 숨겨집니다"
//        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // 안전마커 표시/숨김 상태 업데이트
    private fun updateSafeMarkersVisibility() {
        roadViewModel.roads.value?.forEachIndexed { index, roadData ->
            val marker = markers.getOrNull(index) ?: return@forEachIndexed

            // 안전마커 (totalScore 0.0 ~ 4.0)만 토글 적용
            if (roadData.totalScore >= 0.0 && roadData.totalScore <= 4.0) {
                marker.map = if (isSafeMarkersVisible) naverMap else null
            }
        }
    }

    // 안전마커 토글 버튼 상태 업데이트
    private fun updateSafeMarkerToggleButton(button: androidx.appcompat.widget.AppCompatButton) {
        button.isSelected = isSafeMarkersVisible
        button.text = if (isSafeMarkersVisible) "안전마커 숨기기" else "안전마커 보이기"
    }

}

