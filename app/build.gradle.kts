
import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    val versionPropsFile = rootProject.file("version.properties")
    val versionProps = Properties()
    if (versionPropsFile.exists()) {
        versionProps.load(FileInputStream(versionPropsFile))
    }
    val major = versionProps["version.major"]?.toString()?.toInt() ?: 1
    val minor = versionProps["version.minor"]?.toString()?.toInt() ?: 0
    val patch = versionProps["version.patch"]?.toString()?.toInt() ?: 0
    val build = versionProps["version.build"]?.toString()?.toInt() ?: 1

    namespace = "com.andrefdias.dailynote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.andrefdias.dailynote"
        minSdk = 29
        targetSdk = 36
        versionCode = build
        versionName = "$major.$minor.$patch"

        buildConfigField("String", "BUILD_TIME", "\"${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        externalNativeBuild {
            cmake {
                arguments("-DCMAKE_SHARED_LINKER_FLAGS=-Wl,-z,max-page-size=16384")
            }
        }
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
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Serialization (used in RoomOcorrenciaRepository for JSON type converters)
    implementation(libs.kotlinx.serialization.json)

    // OkHttp (used by GoogleDriveBackupService for Drive REST API calls)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.patrykandpatrick.vico:compose:1.14.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
    // Osmdroid
    
    // Osmdroid
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    
    // MapLibre
    implementation("org.maplibre.gl:android-sdk:11.4.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.0")

    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.4")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.play.services.auth)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Jetpack Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    compileOnly(libs.errorprone.annotations)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    // Google Play Services & Hardware APIs
    implementation(libs.play.services.location)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Exportação Excel
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}
