plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "jp.ac.gifu_u.info.takaharu.myapplication_guitar_sound"
    compileSdk = 35

    defaultConfig {
        applicationId = "jp.ac.gifu_u.info.takaharu.myapplication_guitar_sound"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    implementation(files("libs/TarsosDSP.jar"))
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}