plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.example.devmour"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.devmour"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    
    lint {
        disable += "AppLinkUrlError"
    }
}

dependencies {

    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.22.1")

        // ViewModel과 viewModelScope를 위한 의존성
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")


        // Retrofit 관련 의존성
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
// JSON 처리용
    implementation("com.google.code.gson:gson:2.10.1")
// 코루틴 지원
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.kakao.sdk:v2-all:2.20.1") // 전체 모듈 설치, 2.11.0 버전부터 지원

    // 네이버 OAuth
    implementation("com.navercorp.nid:oauth:5.9.0") // 최신 안정버전으로 교체 가능

    // HTTP 클라이언트 (네이버 API 호출용)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 구글
    implementation("com.google.android.gms:play-services-auth:21.2.0") // 최신 버전 확인 가능
    //room
    kapt("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // WorkManager (Kotlin Coroutine용)
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    // NotificationCompat
    implementation("androidx.core:core-ktx:1.13.1")
    // ... 기존 의존성

    // OkHttp & Logging Interceptor
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp & Logging
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")

}