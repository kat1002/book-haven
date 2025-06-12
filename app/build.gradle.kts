plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.son.bookhaven"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.son.bookhaven"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core AndroidX and Material libraries
    implementation(libs.appcompat)
    implementation(libs.material) // Assumes libs.material points to 1.10.0 or newer
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Navigation components
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment) // Assumes these point to stable versions like 2.7.0 or newer
    implementation(libs.navigation.ui)      // Assumes these point to stable versions like 2.7.0 or newer

    // Additional support libraries
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.code.gson:gson:2.11.0")

    // Testing
    testImplementation(libs.junit) // Assumes this points to junit:junit:4.13.2 or newer

    androidTestImplementation(libs.ext.junit)      // Assumes this points to androidx.test.ext:junit:1.1.5 or newer
    androidTestImplementation(libs.espresso.core) // Assumes this points to androidx.test.espresso:espresso-core:3.5.1 or newer
    // No need for the explicit androidx.test core, runner, or older espresso alpha versions if libs.ext.junit and libs.espresso.core are up-to-date.
}