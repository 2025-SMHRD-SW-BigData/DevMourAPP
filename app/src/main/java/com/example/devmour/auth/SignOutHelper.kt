package com.example.devmour.auth

import android.content.Context
import android.util.Log
import com.kakao.sdk.user.UserApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin

object SignOutHelper {
    
    // 모든 OAuth 제공자에서 로그아웃
    fun signOutAll(context: Context, onComplete: () -> Unit) {
        Log.d("SignOutHelper", "모든 OAuth 제공자에서 로그아웃 시작")
        
        var completedCount = 0
        val totalProviders = 3
        
        // 완료 콜백
        fun checkCompletion() {
            completedCount++
            if (completedCount >= totalProviders) {
                Log.d("SignOutHelper", "모든 OAuth 제공자 로그아웃 완료")
                onComplete()
            }
        }
        
        // 카카오 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("SignOutHelper", "카카오 로그아웃 실패", error)
            } else {
                Log.d("SignOutHelper", "카카오 로그아웃 성공")
            }
            checkCompletion()
        }
        
        // 네이버 로그아웃
        try {
            NaverIdLoginSDK.logout()
            Log.d("SignOutHelper", "네이버 로그아웃 성공")
        } catch (e: Exception) {
            Log.e("SignOutHelper", "네이버 로그아웃 실패", e)
        }
        checkCompletion()
        
        // 구글 로그아웃
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                Log.d("SignOutHelper", "구글 로그아웃 성공")
                checkCompletion()
            }.addOnFailureListener { e ->
                Log.e("SignOutHelper", "구글 로그아웃 실패", e)
                checkCompletion()
            }
        } catch (e: Exception) {
            Log.e("SignOutHelper", "구글 로그아웃 초기화 실패", e)
            checkCompletion()
        }
    }
}
