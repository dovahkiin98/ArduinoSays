import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "net.inferno.compose"
        minSdkVersion(28)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        setCoreLibraryDesugaringEnabled(true)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

//    buildFeatures.compose true
}

dependencies {
//    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.2.0-rc01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-rc02")

    implementation("com.google.android.material:material:1.2.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta3")

    implementation("androidx.ui:ui-layout:0.1.0-dev02")
    implementation("androidx.ui:ui-material:0.1.0-dev02")
    implementation("androidx.ui:ui-tooling:0.1.0-dev02")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
