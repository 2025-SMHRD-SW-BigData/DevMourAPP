package com.example.devmour.auth;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class AuthApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 카카오 SDK 초기화
        KakaoSdk.init(this, "bb348933b27cba1d726cf4e152588cb0");
    }
}
