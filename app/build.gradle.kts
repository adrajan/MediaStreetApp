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

    defaultConfig {
        applicationId = "ads.mediastreet.ai"
        minSdk = 21
        //noinspection ExpiredTargetSdkVersion this for clover Device
        targetSdk = 25
        versionCode = 20
        versionName = "1.0.20"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_URL", "\"https://api.mediastreet.ai\"")
        }
        getByName("debug") {
            buildConfigField("String", "API_URL", "\"https://api.mediastreet.ai\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    useLibrary("org.apache.http.legacy")

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
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
    
    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    implementation(libs.clover.cfp.sdk)
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)
    // Retrofit and login
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
}

// Custom tasks for running tests
tasks.register("runUnitTests") {
    dependsOn("testDebugUnitTest")
    description = "Run all unit tests"
}

tasks.register("runInstrumentedTests") {
    dependsOn("connectedDebugAndroidTest")
    description = "Run all instrumented tests"
}

tasks.register("runAllTests") {
    dependsOn("runUnitTests", "runInstrumentedTests")
    description = "Run both unit tests and instrumented tests"
}