package com.example.devmour

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*

class ReportActivity : AppCompatActivity() {
    
    private lateinit var etAddress: EditText
    private lateinit var btnSearch: TextView
    private lateinit var btnFlood: com.google.android.material.card.MaterialCardView
    private lateinit var btnIce: com.google.android.material.card.MaterialCardView
    private lateinit var btnBreak: com.google.android.material.card.MaterialCardView
    // private lateinit var btnEtc: com.google.android.material.card.MaterialCardView
    private lateinit var btnCamera: com.google.android.material.card.MaterialCardView
    private lateinit var btnGallery: com.google.android.material.card.MaterialCardView
    private lateinit var btnSubmit: com.google.android.material.card.MaterialCardView
    private lateinit var photoContainer: LinearLayout
    
    private var selectedCategory = ""
    private val selectedImages = mutableListOf<Bitmap>()
    private val maxImages = 3
    
    // 서버 설정
    private val SERVER_URL = "http://10.0.2.2:3000" // 에뮬레이터용 localhost
    // 실제 기기 사용 시: "http://[컴퓨터IP]:3000"
    
    companion object {
        private const val CAMERA_REQUEST = 1001
        private const val GALLERY_REQUEST = 1002
        private const val CAMERA_PERMISSION_REQUEST = 1003
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etAddress = findViewById(R.id.et_address)
        btnSearch = findViewById(R.id.btn_search)
        btnFlood = findViewById(R.id.btn_flood)
        btnIce = findViewById(R.id.btn_ice)
        btnBreak = findViewById(R.id.btn_break)
        // btnEtc = findViewById(R.id.btn_etc)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        btnSubmit = findViewById(R.id.btn_submit)
        photoContainer = findViewById(R.id.photo_container)
    }
    
    private fun setupClickListeners() {
        // 검색 버튼
        btnSearch.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                // 주소 검색 로직 (지도 API 연동 시 구현)
                Toast.makeText(this, "주소 검색: $address", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "주소를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 카테고리 버튼들
        btnFlood.setOnClickListener { selectCategory("도로 침수", btnFlood) }
        btnIce.setOnClickListener { selectCategory("도로 빙결", btnIce) }
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
        val buttons = listOf(btnFlood, btnIce, btnBreak)
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
            frameLayout.layoutParams = LinearLayout.LayoutParams(100, 120).apply {
                marginEnd = 8
            }
            
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.setBackgroundResource(R.drawable.edittext_background)
            imageView.elevation = 2f
            imageView.translationZ = 2f
            
            // 삭제 버튼
            val deleteButton = TextView(this)
            deleteButton.text = "×"
            deleteButton.textSize = 16f
            deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            deleteButton.gravity = android.view.Gravity.CENTER
            deleteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            deleteButton.layoutParams = FrameLayout.LayoutParams(24, 24).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
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
                gravity = android.view.Gravity.CENTER
            }
            
            val countText = TextView(this)
            countText.text = "${selectedImages.size}/${maxImages}"
            countText.textSize = 12f
            countText.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            countText.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
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
        
        // 서버로 민원 제출
        submitToServer(address, selectedCategory)
    }
    
    private fun submitToServer(address: String, category: String) {
        // 로딩 메시지 표시
        Toast.makeText(this, "민원을 제출하는 중...", Toast.LENGTH_SHORT).show()
        
        // 코루틴으로 네트워크 작업 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$SERVER_URL/api/reports/submit")
                val connection = url.openConnection() as HttpURLConnection
                
                // 멀티파트 폼 데이터 설정
                val boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString()
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connection.doOutput = true
                connection.doInput = true
                
                val outputStream = connection.outputStream
                val writer = PrintWriter(outputStream)
                
                // 텍스트 데이터 추가
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"addr\"\r\n\r\n")
                writer.append("").append("\r\n") // 주소는 null로 설정
                
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"c_report_detail\"\r\n\r\n")
                writer.append(category).append("\r\n") // 카테고리 정보
                
                // 제보자 정보 (null로 설정)
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"c_reporter_name\"\r\n\r\n")
                writer.append("").append("\r\n") // null
                
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"c_reporter_phone\"\r\n\r\n")
                writer.append("").append("\r\n") // null
                
                // 이미지 데이터 추가
                selectedImages.forEachIndexed { index, bitmap ->
                    val fieldName = "c_report_file${index + 1}"
                    val imageBytes = bitmapToByteArray(bitmap)
                    
                    writer.append("--$boundary\r\n")
                    writer.append("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"image_${index + 1}.jpg\"\r\n")
                    writer.append("Content-Type: image/jpeg\r\n\r\n")
                    writer.flush()
                    
                    outputStream.write(imageBytes)
                    writer.append("\r\n")
                }
                
                writer.append("--$boundary--\r\n")
                writer.flush()
                writer.close()
                
                // 응답 처리
                val responseCode = connection.responseCode
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                
                val response = inputStream.bufferedReader().use { it.readText() }
                
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@ReportActivity, "민원이 성공적으로 제출되었습니다!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@ReportActivity, "민원 제출에 실패했습니다: $response", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}