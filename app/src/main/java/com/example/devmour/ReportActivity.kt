package com.example.devmour

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.UiSettings
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.geometry.LatLng
import android.graphics.Color
import com.example.devmour.data.LocationData
import com.example.devmour.auth.LoginManager
import com.example.devmour.auth.SessionManager
import com.example.devmour.alert.MainActivityAlert
import com.example.devmour.data.AddressSearchResult
import com.example.devmour.viewmodel.GeocodingViewModel
import androidx.lifecycle.ViewModelProvider

class ReportActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var etAddress: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnFlood: com.google.android.material.card.MaterialCardView
    private lateinit var btnBreak: com.google.android.material.card.MaterialCardView
    // private lateinit var btnEtc: com.google.android.material.card.MaterialCardView
    private lateinit var btnCamera: com.google.android.material.card.MaterialCardView
    private lateinit var btnGallery: com.google.android.material.card.MaterialCardView
    private lateinit var btnSubmit: com.google.android.material.card.MaterialCardView
    private lateinit var photoContainer: LinearLayout
    
    private var selectedCategory = ""
    private val selectedImages = mutableListOf<Bitmap>()
    private val maxImages = 1
    private lateinit var naverMap: NaverMap
    
    // 위치 관련 변수들
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION = 1000
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    
    // 검색 관련 변수들
    private val locationMarkers = mutableListOf<Marker>() // 위치 검색 마커
    private var selectedLocation: LocationData? = null // 선택된 위치 정보
    private var isLocationConfirmed = false // 위치 확정 여부
    
    // Geocoding ViewModel
    private lateinit var geocodingViewModel: GeocodingViewModel
    
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
        
        // 남구
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
        
        // 동구
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
        
        // 북구
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
        
        // 서구
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
    
    // 서버 설정
    private val SERVER_URL = "http://175.45.194.114:3001" // 실제 기기용 PC IP
    // 실제 기기 사용 시: "http://[컴퓨터IP]:3000"
    
    companion object {
        private const val CAMERA_REQUEST = 1001
        private const val GALLERY_REQUEST = 1002
        private const val CAMERA_PERMISSION_REQUEST = 1003
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 로그인 상태 확인
        if (!LoginManager.isLoggedIn(this) || !LoginManager.isTokenValid(this)) {
            // 로그인되지 않았거나 토큰이 유효하지 않은 경우 로그인 화면으로 이동
            val intent = Intent(this, com.example.devmour.LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_report)
        
        initViews()
        setupClickListeners()
        initLocation()
        initMap()
        
        // Geocoding ViewModel 초기화
        geocodingViewModel = ViewModelProvider(this)[GeocodingViewModel::class.java]
        observeGeocoding()
    }
    
    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSource = FusedLocationSource(this@ReportActivity, LOCATION_PERMISSION)
    }
    
    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
        mapFragment?.getMapAsync(this)
    }
    
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        
        // 지도 UI 설정
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isZoomControlEnabled = true
        uiSettings.isCompassEnabled = false  // 나침반 비활성화
        uiSettings.isScaleBarEnabled = true
        
        // 위치 소스 설정
        naverMap.locationSource = locationSource
        
        // 위치 추적 설정
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        
        // 현재 위치 가져오기
        getCurrentLocation()
        
        // 내 위치 버튼 클릭 리스너 설정
        setupMyLocationButton()
    }
    
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    
                    // 현재 위치로 카메라 이동
                    val currentLocation = com.naver.maps.geometry.LatLng(currentLatitude, currentLongitude)
                    val cameraUpdate = com.naver.maps.map.CameraUpdate.scrollTo(currentLocation)
                    naverMap.moveCamera(cameraUpdate)
                    
                    Log.d("ReportActivity", "현재 위치: $currentLatitude, $currentLongitude")
                } else {
                    // 위치를 가져올 수 없는 경우 기본 위치로 설정
                    setDefaultLocation()
                }
            }
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            setDefaultLocation()
        }
    }
    
    private fun setDefaultLocation() {
        // 기본 위치 설정 (서울시청)
        currentLatitude = 37.5665
        currentLongitude = 126.9780
        val cameraUpdate = com.naver.maps.map.CameraUpdate.scrollTo(com.naver.maps.geometry.LatLng(currentLatitude, currentLongitude))
        naverMap.moveCamera(cameraUpdate)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION -> {
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (!locationSource.isActivated) {
                    naverMap.locationTrackingMode = LocationTrackingMode.None
                    Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                } else {
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    getCurrentLocation()
                }
            }
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun initViews() {
        etAddress = findViewById(R.id.et_address)
        btnSearch = findViewById(R.id.btn_search)
        btnFlood = findViewById(R.id.btn_flood)
        btnBreak = findViewById(R.id.btn_break)
        // btnEtc = findViewById(R.id.btn_etc)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        btnSubmit = findViewById(R.id.btn_submit)
        photoContainer = findViewById(R.id.photo_container)
        
        // 하단 네비게이션 바
        findViewById<LinearLayout>(R.id.btnNotification).setOnClickListener {
            // 알림내역 페이지로 이동
            val intent = Intent(this, MainActivityAlert::class.java)
            startActivity(intent)
        }
        
        findViewById<LinearLayout>(R.id.btnMain).setOnClickListener {
            // 메인화면으로 이동
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        findViewById<LinearLayout>(R.id.btnReport).setOnClickListener {
            // 현재 페이지 (민원접수) - 아무 동작 안함
            Toast.makeText(this, "현재 민원접수 페이지입니다", Toast.LENGTH_SHORT).show()
        }
        
        // 도로 침수 버튼 클릭 이벤트
        btnFlood.setOnClickListener {
            Log.d("ReportActivity", "도로 침수 버튼 클릭됨")
            selectCategory("flood")
        }
        
        // 도로 파손 버튼 클릭 이벤트
        btnBreak.setOnClickListener {
            Log.d("ReportActivity", "도로 파손 버튼 클릭됨")
            selectCategory("break")
        }
    }
    
    private fun selectCategory(category: String) {
        Log.d("ReportActivity", "selectCategory 호출됨: $category")
        selectedCategory = category
        
        // 모든 버튼을 기본 상태로 초기화
        resetButtonStates()
        
        // 선택된 버튼만 활성화 상태로 변경
        when (category) {
            "flood" -> {
                Log.d("ReportActivity", "도로 침수 버튼 선택됨")
                val linearLayout = btnFlood.getChildAt(0) as? LinearLayout
                if (linearLayout != null) {
                    linearLayout.isSelected = true
                    // TextView 색상 변경
                    for (i in 0 until linearLayout.childCount) {
                        val child = linearLayout.getChildAt(i)
                        if (child is TextView) {
                            child.setTextColor(Color.WHITE)
                            break
                        }
                    }
                }
            }
            "break" -> {
                Log.d("ReportActivity", "도로 파손 버튼 선택됨")
                val linearLayout = btnBreak.getChildAt(0) as? LinearLayout
                if (linearLayout != null) {
                    linearLayout.isSelected = true
                    // TextView 색상 변경
                    for (i in 0 until linearLayout.childCount) {
                        val child = linearLayout.getChildAt(i)
                        if (child is TextView) {
                            child.setTextColor(Color.WHITE)
                            break
                        }
                    }
                }
            }
        }
    }
    
    private fun resetButtonStates() {
        // 도로 침수 버튼을 기본 상태로
        val floodLinearLayout = btnFlood.getChildAt(0) as? LinearLayout
        if (floodLinearLayout != null) {
            floodLinearLayout.isSelected = false
            for (i in 0 until floodLinearLayout.childCount) {
                val child = floodLinearLayout.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(Color.parseColor("#1E2A44"))
                    break
                }
            }
        }
        
        // 도로 파손 버튼을 기본 상태로
        val breakLinearLayout = btnBreak.getChildAt(0) as? LinearLayout
        if (breakLinearLayout != null) {
            breakLinearLayout.isSelected = false
            for (i in 0 until breakLinearLayout.childCount) {
                val child = breakLinearLayout.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(Color.parseColor("#1E2A44"))
                    break
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        // 검색 버튼
        btnSearch.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                // API 우선 검색 시작
                geocodingViewModel.searchAddress(address)
                // 검색 완료 후 키보드 숨기기
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etAddress.windowToken, 0)
                etAddress.clearFocus()
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 엔터키로 검색
        etAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                btnSearch.performClick()
                true
            } else {
                false
            }
        }
        
        // 카테고리 버튼들
        btnFlood.setOnClickListener { selectCategory("도로 침수", btnFlood) }
        btnBreak.setOnClickListener { selectCategory("도로 파손", btnBreak) }
        // btnEtc.setOnClickListener { selectCategory("기타 사항", btnEtc) }
        
        // 사진 촬영
        btnCamera.setOnClickListener {
            if (selectedImages.size >= maxImages) {
                Toast.makeText(this, "최대 ${maxImages}장까지 첨부 가능합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        
        // 앨범 선택
        btnGallery.setOnClickListener {
            if (selectedImages.size >= maxImages) {
                Toast.makeText(this, "최대 ${maxImages}장까지 첨부 가능합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openGallery()
        }
        
        // 제출 버튼
        btnSubmit.setOnClickListener {
            submitReport()
        }
    }
    
    private fun selectCategory(category: String, selectedButton: com.google.android.material.card.MaterialCardView) {
        selectedCategory = category
        
        // 모든 카테고리 버튼을 기본 상태로 리셋
        resetCategoryButtons()
        
        // 선택된 버튼만 강조 (배경색 변경)
        selectedButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.selected_category))
        
        Toast.makeText(this, "$category 선택됨", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetCategoryButtons() {
        val buttons = listOf(btnFlood, btnBreak)
        buttons.forEach { button ->
            button.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun openCamera() {
        try {
            // 실시간 카메라 앱 실행 (사진 촬영용)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            
            // 카메라 앱이 있는지 확인
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            } else {
                // 카메라 앱이 없으면 대체 방법 시도
                val alternativeIntent = Intent("android.media.action.IMAGE_CAPTURE")
                if (alternativeIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(alternativeIntent, CAMERA_REQUEST)
                } else {
                    Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.\n실제 기기에서 테스트해주세요.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "카메라를 열 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openGallery() {
        try {
            // 갤러리에서 사진 선택 (앨범 선택용)
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            
            if (galleryIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryIntent, GALLERY_REQUEST)
            } else {
                // 대체 방법으로 파일 선택기 사용
                val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                if (fileIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(fileIntent, GALLERY_REQUEST)
                } else {
                    Toast.makeText(this, "갤러리 앱을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "갤러리를 열 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let { addImage(it) }
                }
                GALLERY_REQUEST -> {
                    data?.data?.let { uri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            addImage(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun addImage(bitmap: Bitmap) {
        if (selectedImages.size < maxImages) {
            selectedImages.add(bitmap)
            updatePhotoPreview()
            Toast.makeText(this, "사진이 추가되었습니다 (${selectedImages.size}/${maxImages})", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "최대 ${maxImages}장까지 추가할 수 있습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updatePhotoPreview() {
        photoContainer.removeAllViews()
        
        // 선택된 이미지들 표시
        selectedImages.forEachIndexed { index, bitmap ->
            val frameLayout = FrameLayout(this)
            val dp100 = (100 * resources.displayMetrics.density).toInt()
            val dp120 = (120 * resources.displayMetrics.density).toInt()
            val dp8 = (8 * resources.displayMetrics.density).toInt()
            val dp4 = (4 * resources.displayMetrics.density).toInt()
            
            frameLayout.layoutParams = LinearLayout.LayoutParams(dp100, dp120).apply {
                marginEnd = dp8
            }
            frameLayout.setBackgroundResource(R.drawable.edittext_background)
            frameLayout.elevation = 2f
            frameLayout.translationZ = 2f
            
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = FrameLayout.LayoutParams(dp100, dp120)
            imageView.setPadding(dp4, dp4, dp4, dp4)
            imageView.adjustViewBounds = false
            imageView.maxWidth = dp100
            imageView.maxHeight = dp120
            
            // 삭제 버튼
            val deleteButton = TextView(this)
            deleteButton.text = "×"
            deleteButton.textSize = 16f
            deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            deleteButton.gravity = Gravity.CENTER
            deleteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            deleteButton.layoutParams = FrameLayout.LayoutParams(24, 24).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 4
                rightMargin = 4
            }
            deleteButton.setOnClickListener {
                selectedImages.removeAt(index)
                updatePhotoPreview()
                Toast.makeText(this, "사진이 삭제되었습니다 (${selectedImages.size}/${maxImages})", Toast.LENGTH_SHORT).show()
            }
            
            frameLayout.addView(imageView)
            frameLayout.addView(deleteButton)
            photoContainer.addView(frameLayout)
        }
        
        // 남은 슬롯 표시
        val remainingSlots = maxImages - selectedImages.size
        if (remainingSlots > 0) {
            val placeholder = FrameLayout(this)
            placeholder.layoutParams = LinearLayout.LayoutParams(100, 120)
            placeholder.setBackgroundResource(R.drawable.edittext_background)
            placeholder.elevation = 2f
            placeholder.translationZ = 2f
            
            val plusText = TextView(this)
            plusText.text = "+"
            plusText.textSize = 24f
            plusText.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            plusText.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            
            val countText = TextView(this)
            countText.text = "${selectedImages.size}/${maxImages}"
            countText.textSize = 12f
            countText.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            countText.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 32
            }
            
            placeholder.addView(plusText)
            placeholder.addView(countText)
            photoContainer.addView(placeholder)
        }
    }
    
    private fun submitReport() {
        val address = etAddress.text.toString().trim()
        
        if (address.isEmpty()) {
            Toast.makeText(this, "주소를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "제보 유형을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isLocationConfirmed) {
            Toast.makeText(this, "지도에서 위치를 선택하고 등록해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 서버로 민원 제출 (확정된 위치 정보 포함)
        submitToServer(address, selectedCategory, currentLatitude, currentLongitude)
    }
    
    private fun submitToServer(address: String, category: String, latitude: Double, longitude: Double) {
        // 로딩 메시지 표시
        Toast.makeText(this, "민원을 제출하는 중...", Toast.LENGTH_SHORT).show()
        
        // 상세 로그 시작
        Log.d("ReportActivity", "=== 민원 제출 시작 ===")
        Log.d("ReportActivity", "서버 URL: $SERVER_URL")
        Log.d("ReportActivity", "주소: $address")
        Log.d("ReportActivity", "카테고리: $category")
        Log.d("ReportActivity", "위도: $latitude")
        Log.d("ReportActivity", "경도: $longitude")
        Log.d("ReportActivity", "선택된 이미지 수: ${selectedImages.size}")
        
        // 코루틴으로 네트워크 작업 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ReportActivity", "Retrofit 설정 시작...")
                
                // Retrofit 설정
                val retrofit = Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                Log.d("ReportActivity", "Retrofit 설정 완료")
                
                val apiService = retrofit.create(com.example.devmour.api.ApiService::class.java)
                Log.d("ReportActivity", "ApiService 생성 완료")
                
                // RequestBody 생성
                val addrBody = address.toRequestBody("text/plain".toMediaType())
                val detailBody = category.toRequestBody("text/plain".toMediaType())
                val latBody = latitude.toString().toRequestBody("text/plain".toMediaType())
                val lonBody = longitude.toString().toRequestBody("text/plain".toMediaType())
                val nameBody = "".toRequestBody("text/plain".toMediaType())
                val phoneBody = "".toRequestBody("text/plain".toMediaType())
                
                Log.d("ReportActivity", "RequestBody 생성 완료")
                
                // 이미지 파일을 MultipartBody.Part로 변환 (최대 1개)
                var filePart1: MultipartBody.Part? = null
                
                if (selectedImages.isNotEmpty()) {
                    Log.d("ReportActivity", "이미지 처리 시작...")
                    val file = createImageFile(selectedImages[0], 0)
                    Log.d("ReportActivity", "이미지 파일 생성: ${file.absolutePath}")
                    val requestFile = file.asRequestBody("image/jpeg".toMediaType())
                    filePart1 = MultipartBody.Part.createFormData("c_report_file1", file.name, requestFile)
                    Log.d("ReportActivity", "이미지 MultipartBody.Part 생성 완료")
                }
                
                Log.d("ReportActivity", "파일 파트 생성 완료")
                Log.d("ReportActivity", "API 호출 시작...")
                
                // API 호출
                val response = apiService.submitReport(
                    addrBody,
                    detailBody,
                    latBody,
                    lonBody,
                    nameBody,
                    phoneBody,
                    filePart1,
                    null,
                    null
                )
                
                Log.d("ReportActivity", "API 호출 완료")
                Log.d("ReportActivity", "응답 코드: ${response.code()}")
                Log.d("ReportActivity", "응답 메시지: ${response.message()}")
                Log.d("ReportActivity", "응답 성공 여부: ${response.isSuccessful}")
                
                // 원시 응답 본문 로그 출력
                val responseBody = response.errorBody()?.string() ?: response.body()?.toString()
                Log.d("ReportActivity", "원시 응답 본문: $responseBody")
                
                withContext(Dispatchers.Main) {
                    try {
                        if (response.isSuccessful) {
                            val reportResponse = response.body()
                            Log.d("ReportActivity", "응답 본문: $reportResponse")
                            
                            if (reportResponse?.success == true) {
                                Log.d("ReportActivity", "민원 제출 성공! ID: ${reportResponse.data?.reportId}")
                                Toast.makeText(this@ReportActivity, "민원이 성공적으로 제출되었습니다.\n소중한 의견 감사합니다.", Toast.LENGTH_LONG).show()
                                
                                // 잠시 대기 후 MainActivity로 이동 (Toast가 보이도록)
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    try {
                                        // 리소스 정리
                                        cleanupResources()
                                        
                                        val intent = Intent(this@ReportActivity, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    } catch (e: Exception) {
                                        Log.e("ReportActivity", "MainActivity 이동 중 오류: ${e.message}", e)
                                        finish() // 오류 시에도 액티비티 종료
                                    }
                                }, 2000) // 2초 대기
                            } else {
                                Log.e("ReportActivity", "민원 제출 실패: ${reportResponse?.message}")
                                Toast.makeText(this@ReportActivity, "민원 제출에 실패했습니다: ${reportResponse?.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.e("ReportActivity", "서버 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                            Log.e("ReportActivity", "오류 본문: ${response.errorBody()?.string()}")
                            Toast.makeText(this@ReportActivity, "서버 오류: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("ReportActivity", "UI 업데이트 중 오류: ${e.message}", e)
                        Toast.makeText(this@ReportActivity, "처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("ReportActivity", "예외 발생!", e)
                Log.e("ReportActivity", "예외 타입: ${e.javaClass.simpleName}")
                Log.e("ReportActivity", "예외 메시지: ${e.message}")
                Log.e("ReportActivity", "예외 스택 트레이스:", e)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun createImageFile(bitmap: Bitmap, index: Int): File {
        val file = File(cacheDir, "image_${index + 1}_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
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
            showLocationConfirmDialog(location)
            true
        }
        
        locationMarkers.add(marker)
        
        // 카메라를 해당 위치로 이동
        val cameraUpdate = com.naver.maps.map.CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
        naverMap.moveCamera(cameraUpdate)
        
        Log.d("ReportActivity", "검색된 위치로 이동: ${location.name} (${location.latitude}, ${location.longitude})")
    }
    
    // 위치 마커들 제거
    private fun clearLocationMarkers() {
        locationMarkers.forEach { marker ->
            marker.map = null
        }
        locationMarkers.clear()
    }
    
    // Geocoding Observer 설정
    private fun observeGeocoding() {
        Log.d("ReportActivity", "=== Geocoding Observer 설정 시작 ===")

        geocodingViewModel.searchResult.observe(this) { searchResult ->
            searchResult?.let { result ->
                Log.d("ReportActivity", "주소 검색 결과: ${result.address} -> ${result.latitude}, ${result.longitude}")

                // 검색된 위치로 이동
                moveToSearchedLocation(result)

                Toast.makeText(this,
                    "${result.address}로 이동했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        geocodingViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.e("ReportActivity", "API 주소 검색 실패: $it")

                // API 검색 실패 시 로컬 검색으로 fallback
                performLocalSearch()
            }
        }

        geocodingViewModel.isLoading.observe(this) { isLoading ->
            Log.d("ReportActivity", "주소 검색 로딩 상태: $isLoading")
            // 로딩 인디케이터 표시/숨김 처리 가능
        }
    }
    
    // 로컬 검색을 수행하는 별도 함수 추가
    private fun performLocalSearch() {
        val searchQuery = etAddress.text.toString().trim()

        if (searchQuery.isNotEmpty()) {
            val localResults = searchLocations(searchQuery)
            if (localResults.isNotEmpty()) {
                // 로컬 검색 결과가 있으면 첫 번째 결과로 이동
                moveToLocation(localResults.first())
                Toast.makeText(this,
                    "로컬 검색: ${localResults.first().name}으로 이동했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 로컬 검색 결과도 없으면 에러 메시지
                Toast.makeText(this,
                    "검색 결과가 없습니다",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    // API 검색 결과로 이동
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
        marker.width = 140
        marker.height = 140
        marker.captionText = searchResult.address

        marker.tag = "SEARCHED_LOCATION"

        // 마커 클릭 이벤트 - 위치 등록 다이얼로그 표시
        marker.setOnClickListener { _ ->
            showLocationConfirmDialog(LocationData(
                searchResult.address,
                searchResult.latitude,
                searchResult.longitude,
                "검색",
                null
            ))
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
        naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(cameraPosition.target))

        // 위치는 마커 클릭 시 확정되도록 함 (자동 확정 제거)

        Log.d("ReportActivity", "API 검색된 위치로 이동: ${searchResult.address} (${searchResult.latitude}, ${searchResult.longitude})")
    }
    
    // 내 위치 버튼 설정
    private fun setupMyLocationButton() {
        // 내 위치 버튼 활성화
        naverMap.uiSettings.isLocationButtonEnabled = true
        
        // 지도 클릭 리스너 설정
        naverMap.setOnMapClickListener { point, coord ->
            // 현재 위치 근처를 클릭했는지 확인
            if (isNearCurrentLocation(coord.latitude, coord.longitude)) {
                // 현재 위치 마커 표시
                showCurrentLocationMarker(coord.latitude, coord.longitude)
            }
        }
    }
    
    // 현재 위치 가져오기 (마커 포함)
    private fun getCurrentLocationWithMarker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    
                    // 기존 위치 마커들 제거
                    clearLocationMarkers()
                    
                    // 현재 위치 마커 추가
                    val marker = com.naver.maps.map.overlay.Marker()
                    marker.position = com.naver.maps.geometry.LatLng(latitude, longitude)
                    marker.map = naverMap
                    
                    // 현재 위치 마커 스타일 설정
                    marker.icon = com.naver.maps.map.util.MarkerIcons.BLUE
                    marker.iconTintColor = android.graphics.Color.BLUE
                    marker.width = 140
                    marker.height = 140
                    marker.captionText = "현재 위치"
                    
                    marker.tag = "CURRENT_LOCATION"
                    
                    // 마커 클릭 이벤트 - 위치 등록 다이얼로그 표시
                    marker.setOnClickListener { _ ->
                        showLocationConfirmDialog(LocationData(
                            "현재 위치",
                            latitude,
                            longitude,
                            "현재위치",
                            null
                        ))
                        true
                    }
                    
                    locationMarkers.add(marker)
                    
                    // 현재 위치로 카메라 이동
                    val currentLocation = com.naver.maps.geometry.LatLng(latitude, longitude)
                    val cameraUpdate = com.naver.maps.map.CameraUpdate.scrollTo(currentLocation)
                    naverMap.moveCamera(cameraUpdate)
                    
                    Log.d("ReportActivity", "현재 위치 마커 추가: $latitude, $longitude")
                } else {
                    Toast.makeText(this, "현재 위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 현재 위치 근처인지 확인
    private fun isNearCurrentLocation(lat: Double, lng: Double): Boolean {
        if (currentLatitude == 0.0 || currentLongitude == 0.0) return false
        
        val distance = calculateDistance(currentLatitude, currentLongitude, lat, lng)
        return distance < 100 // 100미터 이내
    }
    
    // 두 좌표 간의 거리 계산 (미터)
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    // 현재 위치 마커 표시
    private fun showCurrentLocationMarker(latitude: Double, longitude: Double) {
        // 기존 위치 마커들 제거
        clearLocationMarkers()
        
        // 현재 위치 마커 추가
        val marker = com.naver.maps.map.overlay.Marker()
        marker.position = com.naver.maps.geometry.LatLng(latitude, longitude)
        marker.map = naverMap
        
        // 현재 위치 마커 스타일 설정
        marker.icon = com.naver.maps.map.util.MarkerIcons.BLUE
        marker.iconTintColor = android.graphics.Color.BLUE
        marker.width = 140
        marker.height = 140
        marker.captionText = "현재 위치"
        
        marker.tag = "CURRENT_LOCATION"
        
        // 마커 클릭 이벤트 - 위치 등록 다이얼로그 표시
        marker.setOnClickListener { _ ->
            showLocationConfirmDialog(LocationData(
                "현재 위치",
                latitude,
                longitude,
                "현재위치",
                null
            ))
            true
        }
        
        locationMarkers.add(marker)
        
        Log.d("ReportActivity", "현재 위치 마커 표시: $latitude, $longitude")
    }
    
    // 위치 확정 다이얼로그 표시
    private fun showLocationConfirmDialog(location: LocationData) {
        val message = when (location.type) {
            "구" -> "🏛️ ${location.name}\n\n위치: ${location.latitude}, ${location.longitude}\n\n이 위치를 민원 제출 위치로 등록하시겠습니까?"
            "동" -> "🏘️ ${location.name}\n📍 소속: ${location.parent}\n\n위치: ${location.latitude}, ${location.longitude}\n\n이 위치를 민원 제출 위치로 등록하시겠습니까?"
            "검색" -> "🔍 ${location.name}\n\n위치: ${location.latitude}, ${location.longitude}\n\n이 위치를 민원 제출 위치로 등록하시겠습니까?"
            "현재위치" -> "📍 ${location.name}\n\n위치: ${location.latitude}, ${location.longitude}\n\n이 위치를 민원 제출 위치로 등록하시겠습니까?"
            else -> "${location.name}\n\n위치: ${location.latitude}, ${location.longitude}\n\n이 위치를 민원 제출 위치로 등록하시겠습니까?"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("위치 등록")
            .setMessage(message)
            .setPositiveButton("등록") { _, _ ->
                confirmLocation(location)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    // 위치 확정 처리
    private fun confirmLocation(location: LocationData) {
        selectedLocation = location
        isLocationConfirmed = true
        
        // 현재 위치 업데이트
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        
        // 주소 입력창에 위치명 설정
        etAddress.setText(location.name)
        
        // 성공 메시지 표시
        val message = when (location.type) {
            "구" -> "🏛️ ${location.name} 위치가 등록되었습니다"
            "동" -> "🏘️ ${location.name} (${location.parent}) 위치가 등록되었습니다"
            else -> "${location.name} 위치가 등록되었습니다"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        Log.d("ReportActivity", "위치 확정: ${location.name} (${location.latitude}, ${location.longitude})")
    }
    
    // 리소스 정리
    private fun cleanupResources() {
        try {
            // 선택된 이미지들 정리
            selectedImages.clear()
            
            // 위치 마커들 정리
            clearLocationMarkers()
            
            // 위치 정보 초기화
            selectedLocation = null
            isLocationConfirmed = false
            
            Log.d("ReportActivity", "리소스 정리 완료")
        } catch (e: Exception) {
            Log.e("ReportActivity", "리소스 정리 중 오류: ${e.message}", e)
        }
    }
    
    // 뒤로가기 버튼 처리
    override fun onBackPressed() {
        // 메인화면으로 이동
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 리소스 정리
        cleanupResources()
        
        // 앱이 정상 종료될 때 정리 작업 수행
        SessionManager.markCleanExit(this, true)
    }
    
}