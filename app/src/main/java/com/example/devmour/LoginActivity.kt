package com.example.devmour

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 여기에 추가
        val SKIP_LOGIN = true  // 개발 중일 때 true로 설정

        if (SKIP_LOGIN) {
            val intent = Intent(this, ReportActivity::class.java)  // 민원제보 액티비티
            startActivity(intent)
            finish()
            return  // 아래 기존 로그인 코드들은 실행되지 않음
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 키 해시 출력 (카카오 개발자 콘솔에 등록할 키 해시) - 한 번만 출력
        try {
            val keyHash = com.kakao.sdk.common.util.Utility.getKeyHash(this)
            Log.d("test", "카카오 keyhash: $keyHash")
        } catch (e: Exception) {
            Log.e("test", "카카오 키 해시 생성 실패: ${e.message}")
        }
        
        // 카카오 로그인 버튼 클릭 리스너
        val btnKakaoLogin = findViewById<android.widget.ImageButton>(R.id.btn_kakao)
        
        // callback은 이메일 로그인의 콜백이며, 로그인이 두 곳에서 사용되므로 callback을 변수로 만들어 코드를 간소화했다
        val callback: (com.kakao.sdk.auth.model.OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "카카오 계정으로 로그인 실패!", Toast.LENGTH_SHORT).show()
                Log.e("test", "카카오 계정으로 로그인 실패! 에러: " + error.message)
                Log.e("test", "에러 타입: " + error.javaClass.simpleName)
                Log.e("test", "전체 에러: " + error.toString())
            } else if (token != null) {
                Toast.makeText(this, "카카오 계정으로 로그인 성공!", Toast.LENGTH_SHORT).show()
                Log.d("test", "카카오 계정으로 로그인 성공! " + token.accessToken)
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        
        btnKakaoLogin.setOnClickListener {
            Log.d("test", "카카오 로그인 버튼 클릭됨")
            
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                Log.d("test", "카카오톡 앱 설치됨 - 카카오톡으로 로그인 시도")
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "카카오톡으로 로그인 실패!", Toast.LENGTH_SHORT).show()
                        Log.e("test", "카카오톡으로 로그인 실패! 에러: " + error.message)
                        Log.e("test", "에러 타입: " + error.javaClass.simpleName)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            Log.d("test", "사용자가 로그인을 취소함")
                            return@loginWithKakaoTalk
                        }
                    } else if (token != null) {
                        Toast.makeText(this, "카카오톡으로 로그인 성공!", Toast.LENGTH_SHORT).show()
                        Log.d("test", "카카오톡으로 로그인 성공! 토큰: " + token.accessToken)
                        val intent = Intent(this, ReportActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                Log.d("test", "카카오톡 앱 미설치 - 카카오계정으로 로그인 시도")
                // kakao로 로그인 하지 못할 경우 계정으로 로그인 시도
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        val  btnNaverLogin = findViewById<ImageButton>(R.id.btn_naver)
        btnNaverLogin.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
                override fun onSuccess() {
                    val token = NaverIdLoginSDK.getAccessToken()
                    Log.d("test", "네이버 로그인 성공: accessToken=$token")
                    val intent = Intent(this@LoginActivity, ReportActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    Log.e("test", "네이버 로그인 실패 : $message")
                }

                override fun onError(errorCode: Int, message: String) {
                    Log.e("test", "네이버 로그인 에러: $message")
                }
            })
        }

        // ✅ 구글 로그인 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.google_client_id)) // Web Client ID
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 구글 로그인 버튼 클릭 리스너
        val btnGoogleLogin = findViewById<android.widget.ImageButton>(R.id.googleButton)
        btnGoogleLogin.setOnClickListener {
            Log.d("test", "구글 로그인 버튼 클릭됨")
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                val email = account.email
                val idToken = account.idToken

                Log.d("test", "구글 로그인 성공: email=$email, idToken=$idToken")
                Toast.makeText(this, "구글 로그인 성공: $email", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: ApiException) {
                Log.e("test", "구글 로그인 실패, 코드=${e.statusCode}, 메시지=${e.message}")
                Toast.makeText(this, "구글 로그인 실패: 코드=${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}