package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.devmour.auth.SessionManager
import com.example.devmour.auth.SignOutHelper
import com.example.devmour.auth.LoginManager

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ 기본 스플래시와 연결 (시간 최소화)
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // WebView로 GIF 애니메이션 로드
        val webView = findViewById<WebView>(R.id.webview_loading)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // HTML로 GIF를 감싸서 로드
        val html = """
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: transparent;
                        overflow: hidden;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                    }
                    img {
                        width: 100vw;
                        height: 100vh;
                        object-fit: cover;
                        object-position: center;
                    }
                </style>
            </head>
            <body>
                <img src="file:///android_asset/roro.gif" />
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
        
        // WebView 로드 완료 시 기본 스플래시 즉시 숨기기
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 커스텀 스플래시가 준비되면 기본 스플래시 즉시 숨기기
                splashScreen.setKeepOnScreenCondition { false }
            }
        }
        
        // 로딩 화면을 5초간 표시
        Handler(Looper.getMainLooper()).postDelayed({
            // 지난 세션이 비정상 종료였다면 강제 로그아웃
            if (SessionManager.shouldForceLogout(this)) {
                // 로그인 상태가 있는 경우에만 강제 로그아웃 실행
                if (LoginManager.isLoggedIn(this)) {
                    SignOutHelper.signOutAll(this) {
                        // 모든 OAuth 제공자 로그아웃 완료 후
                        LoginManager.logout(this) // 로컬 로그인 상태도 초기화
                        SessionManager.clearAll(this)
                        goMain()
                    }
                } else {
                    // 로그인 상태가 없으면 바로 메인으로
                    goMain()
                }
            } else {
                // 다음 종료를 대비해 일단 비정상 종료로 마킹(앱이 정상 종료 시 true로 바꿔줌)
                SessionManager.markCleanExit(this, false)
                
                // 항상 메인 화면으로 이동 (로그인 상태는 MainActivity에서 확인)
                goMain()
            }
        }, 5000) // 5초 지연
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    private fun goMain() { 
        startActivity(Intent(this, MainActivity::class.java))
        finish() 
    }
}
