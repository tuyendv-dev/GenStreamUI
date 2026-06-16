plugins {
    alias(libs.plugins.android.library)
}

/**
 * Engine Moonlight (vendored từ moonlight-android, namespace com.limelight) dạng library module.
 * Native build bằng **ndk-build** (Android.mk/Application.mk) — gồm moonlight-common-c + opus + openssl
 * + enet (prebuilt static libs có sẵn trong jni). Patch serverPortBase nằm trong moonlight-common-c.
 */
android {
    namespace = "com.limelight"
    compileSdk = 36
    ndkVersion = "27.0.12077973"

    defaultConfig {
        minSdk = 24

        // Moonlight code tham chiếu các field này (vốn do flavor/app sinh ra).
        buildConfigField("String", "APPLICATION_ID", "\"network.ermis.genstreamui\"")
        buildConfigField("boolean", "ROOT_BUILD", "false")

        externalNativeBuild {
            ndkBuild {
                // nonRoot flavor truyền cờ này cho ndk-build.
                arguments("PRODUCT_FLAVOR=nonRoot")
            }
        }
        ndk {
            // Thu hẹp ABI để build nhanh (thiết bị thật arm64 + emulator x86_64). Thêm armeabi-v7a/x86 sau nếu cần.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.okhttp)
    implementation("org.jcodec:jcodec:0.2.5")
    implementation("org.jmdns:jmdns:3.5.9")
    implementation("com.github.cgutman:ShieldControllerExtensions:1.0.1")
}
