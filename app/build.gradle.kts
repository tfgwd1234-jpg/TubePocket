plugins {
    alias(libs.plugins.android.application)
    // [수정] 아래와 같이 간단하게 id만 명시하면 위에서 정의한 버전을 사용합니다.
    id("com.google.gms.google-services")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\Rich\\AndroidStudioProjects\\TubePocket_release_key")
            keyAlias = "TubePocket_release_key"
            storePassword = "adINsu7322@"
            keyPassword = "adINsu7322@"
        }
    }
    namespace = "com.joo.tubepocket"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.joo.tubepocket"
        minSdk = 26
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
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // [에러 해결을 위해 추가된 코드] Firebase 라이브러리들의 버전을 자동으로 관리해주는 BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    // Firestore 라이브러리
    implementation("com.google.firebase:firebase-firestore")
}