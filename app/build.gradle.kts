plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.dev3mk.awraqi"
    compileSdk = 36
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "com.dev3mk.awraqi"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.4.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("C:\\Users\\mmmme\\Downloads\\awraqiKey.jks")
            storePassword = "12345678"
            keyAlias = "awraqikey"
            keyPassword = "12345678"
        }
        create("release") {
            storeFile = file("C:/Users/mmmme/Downloads/awraqiKey.jks") // adjust path if needed
            storePassword = "12345678"
            keyAlias = "awraqikey"
            keyPassword = "12345678"
            storeType = "PKCS12" // IMPORTANT
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)


    //room database
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //Gson
    implementation(libs.gson)

    //koin
    implementation(libs.koin.android)


    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //fireBase
    implementation(platform(libs.firebase.bom.v3340))
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation (libs.firebase.messaging.ktx)

    //Dimen
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    //flexLayout
    implementation(libs.flexbox)

    //pdfView
    implementation(libs.android.pdf.viewer)

    //glide
    implementation(libs.glide)

    //DataStore
    implementation(libs.androidx.datastore.preferences)

    //swiperefreshlayout
    implementation(libs.androidx.swiperefreshlayout)

    //keyboardVisibilityEvent
    implementation(libs.keyboardvisibilityevent)

    //splashScreen
    implementation(libs.androidx.core.splashscreen)

}