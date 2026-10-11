plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.quanlycongvieccanhan"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.quanlycongvieccanhan"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // Dùng annotationProcessor thay vì KSP/KAPT
    annotationProcessor("androidx.room:room-compiler:$room_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
}