import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
}

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}

android {
    namespace  = "com.telegramvault"
    compileSdk = 35

    defaultConfig {
        applicationId  = "com.telegramvault.app"
        minSdk         = 21
        targetSdk      = 35
        versionCode    = 1
        versionName    = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled           = true
        vectorDrawables.useSupportLibrary = true
        ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64") }

        buildConfigField("int",    "TG_API_ID",
            localProps.getProperty("tg.api.id", "0"))
        buildConfigField("String", "TG_API_HASH",
            "\"${localProps.getProperty("tg.api.hash", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable        = true
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures { viewBinding = true; buildConfig = true }

    compileOptions {
        sourceCompatibility            = JavaVersion.VERSION_17
        targetCompatibility            = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn", "-Xjvm-default=all")
    }

    sourceSets { getByName("main") { jniLibs.srcDirs("src/main/jniLibs") } }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}", "META-INF/DEPENDENCIES",
                "META-INF/LICENSE", "META-INF/LICENSE.txt", "META-INF/NOTICE"
            )
        }
    }

    lint { abortOnError = false; checkReleaseBuilds = false }
    testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    implementation(libs.gson)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.biometric)
    implementation(libs.security.crypto)
    implementation(libs.play.services.auth)
    implementation(libs.workmanager)
    implementation(libs.datastore)
    implementation(libs.splashscreen)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.viewpager2)
    implementation(libs.lottie)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.android)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental",    "true")
    }
}
