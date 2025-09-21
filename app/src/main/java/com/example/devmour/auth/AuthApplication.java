package com.example.devmour.auth;

import android.app.Application;
import android.util.Log;
import com.kakao.sdk.common.KakaoSdk;
import com.navercorp.nid.NaverIdLoginSDK;
import com.example.devmour.R;

public class AuthApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 카카오 SDK 초기화
        KakaoSdk.init(this, "bb348933b27cba1d726cf4e152588cb0");
        
        // 네이버 로그인 SDK 초기화 (Application Context만 전달)
        NaverIdLoginSDK.INSTANCE.initialize(
                this,
                getString(R.string.naver_client_id),
                getString(R.string.naver_client_secret),
                getString(R.string.app_name)
        );
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        // 앱이 정상 종료될 때 정리 작업 수행
        Log.d("AuthApplication", "앱 정상 종료됨");
        // 여기서는 Application Context를 사용할 수 없으므로 
        // 실제 정리 작업은 각 Activity에서 수행해야 함
    }
}
