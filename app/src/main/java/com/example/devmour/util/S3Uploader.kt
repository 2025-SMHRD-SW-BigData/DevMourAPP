package com.example.devmour.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.devmour.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class S3Uploader {
    
    companion object {
        private const val TAG = "S3Uploader"
        
        // AWS 설정 - BuildConfig에서 가져옴
        private val ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY_ID
        private val SECRET_KEY = BuildConfig.AWS_SECRET_ACCESS_KEY
        private val BUCKET_NAME = BuildConfig.AWS_S3_BUCKET_NAME
        private val REGION = BuildConfig.AWS_REGION
        
        init {
            // 필수 설정 검증
            if (ACCESS_KEY.isNullOrEmpty() || SECRET_KEY.isNullOrEmpty()) {
                throw IllegalStateException("AWS 자격 증명이 설정되지 않았습니다. 환경 변수를 확인하세요.")
            }
        }
    }
    
    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        AmazonS3Client(credentials, Region.getRegion(Regions.fromName(REGION)))
    }
    
    /**
     * Bitmap을 S3에 업로드하고 URL을 반환
     */
    suspend fun uploadBitmap(
        bitmap: Bitmap,
        folder: String = "reports",
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "S3 업로드 시작")
            
            // Bitmap을 ByteArray로 변환
            val byteArray = bitmapToByteArray(bitmap)
            Log.d(TAG, "이미지 크기: ${byteArray.size} bytes")
            
            // 파일명 생성
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${folder}/${timestamp}_${System.currentTimeMillis()}.jpg"
            Log.d(TAG, "업로드 파일명: $fileName")
            
            // 메타데이터 설정
            val metadata = ObjectMetadata().apply {
                contentLength = byteArray.size.toLong()
                contentType = "image/jpeg"
            }
            
            // S3 업로드 요청 생성
            val putObjectRequest = PutObjectRequest(
                BUCKET_NAME,
                fileName,
                ByteArrayInputStream(byteArray),
                metadata
            )
            
            // S3에 업로드
            Log.d(TAG, "S3 업로드 실행 중...")
            s3Client.putObject(putObjectRequest)
            
            // 업로드된 파일의 URL 생성
            val imageUrl = "https://${BUCKET_NAME}.s3.${REGION}.amazonaws.com/${fileName}"
            Log.d(TAG, "S3 업로드 성공: $imageUrl")
            
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "S3 업로드 실패", e)
            Result.failure(e)
        }
    }
    
    /**
     * Bitmap을 ByteArray로 변환
     */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
    
    /**
     * 여러 이미지를 순차적으로 업로드
     */
    suspend fun uploadMultipleBitmaps(
        bitmaps: List<Bitmap>,
        folder: String = "reports",
        context: Context
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val urls = mutableListOf<String>()
            
            bitmaps.forEachIndexed { index, bitmap ->
                Log.d(TAG, "이미지 ${index + 1}/${bitmaps.size} 업로드 중...")
                val result = uploadBitmap(bitmap, folder, context)
                
                if (result.isSuccess) {
                    urls.add(result.getOrThrow())
                } else {
                    Log.e(TAG, "이미지 ${index + 1} 업로드 실패: ${result.exceptionOrNull()?.message}")
                    return@withContext Result.failure(result.exceptionOrNull() ?: Exception("업로드 실패"))
                }
            }
            
            Log.d(TAG, "모든 이미지 업로드 완료: ${urls.size}개")
            Result.success(urls)
            
        } catch (e: Exception) {
            Log.e(TAG, "다중 이미지 업로드 실패", e)
            Result.failure(e)
        }
    }
}
