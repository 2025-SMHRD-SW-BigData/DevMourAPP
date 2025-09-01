package com.example.devmour.auth;

import android.app.Application;
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
}
