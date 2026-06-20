import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "network.ermis.genstreamui"
    compileSdk = 36

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "com.gen.play.io.cloud.phone"
            storePassword = "com.gen.play.io.cloud.phone"
            storeFile = File(projectDir, "src/key_store_app.jks")
        }
    }

    defaultConfig {
        applicationId = "network.ermis.genstreamui"
        minSdk = 24
        targetSdk = 36
        versionCode = 14
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Backend base URL
        buildConfigField("String", "baseUrl", "\"https://staging-api.genstream.io\"")
        // OAuth Web Client ID cho Google Sign-In — TODO: thay bằng client id thật của ermis
        buildConfigField("String", "serverClientId", "\"YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Đặt tên file APK output: <tên app>_v<versionName>(<versionCode>)_<yyyyMMdd>_<buildType>.apk
    // Ví dụ: GenStreamUI_v1.0(12)_20260618_release.apk
    val appName = "GenStream"
    applicationVariants.all {
        val variant = this
        val buildDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        variant.outputs.all {
            val output = this as BaseVariantOutputImpl
            output.outputFileName =
                "${appName}_v${variant.versionName}(${variant.versionCode})_${buildDate}_${variant.buildType.name}.apk"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ViewPager2 (màn PreviewMedia vuốt qua lại ảnh/video)
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Media3 ExoPlayer (trình phát trailer; HLS m3u8)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking - Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle / Fragment
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Room
    // Google Sign-In
    implementation(libs.play.services.auth)

    // Crypto - BouncyCastle (sinh client cert PEM cho token-auth / mTLS)
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.bouncycastle.bcprov)

    // Engine Moonlight (native streaming) — namespace com.limelight
    implementation(project(":moonlight"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}