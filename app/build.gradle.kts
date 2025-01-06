plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ads.mediastreet.ai"
    compileSdk = 34


    signingConfigs {
        create("release") {
            keyAlias = "mediastreet"
            keyPassword = "Clover@2025"
            storeFile = file("mediastreet.jks")
            storePassword = "Clover@2025"
            enableV1Signing = true
            enableV2Signing = false
            enableV3Signing = false
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    defaultConfig {
        applicationId = "ads.mediastreet.ai"
        minSdk = 21
        //noinspection ExpiredTargetSdkVersion this for clover Device
        targetSdk = 25
        versionCode = 11
        versionName = "1.0.11"

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
    useLibrary("org.apache.http.legacy")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    // Clover SDK
    implementation("com.clover.sdk:clover-android-sdk:316")
    implementation("com.clover.sdk:clover-android-connector-sdk:316")
    // Glide dependency
    implementation(libs.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.clover.cfp.sdk)
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)
    // Retrofit and login
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)


}