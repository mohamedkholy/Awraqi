plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dev3mk.awraqi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dev3mk.awraqi"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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