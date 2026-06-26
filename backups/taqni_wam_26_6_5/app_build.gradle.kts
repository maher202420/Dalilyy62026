plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.Serviseyem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.Serviseyem"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val geminiKey = System.getenv("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose (Explicit Versions)
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Networking & Serialization (Official Retrofit 2.11.0)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Lifecycle compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Firebase (Explicit Versions)
    implementation("com.google.firebase:firebase-common:20.4.3")
    implementation("com.google.firebase:firebase-firestore:24.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}

tasks.register("copyApkToRoot") {
    dependsOn("assembleDebug")
    doLast {
        val srcFile = file("${layout.buildDirectory.get().asFile}/outputs/apk/debug/app-debug.apk")
        val destFile = file("${project.rootDir}/app-debug.apk")
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, overwrite = true)
            println("APK copied successfully to ${destFile.absolutePath}")
        } else {
            error("Source APK not found at ${srcFile.absolutePath}")
        }
    }
}

tasks.register("backupTaqniWam") {
    doLast {
        val backupDir = File("${project.rootDir}/backups/taqni_wam_26_6_5")
        backupDir.deleteRecursively()
        backupDir.mkdirs()
        
        // Copy Java/Kotlin Sources
        val srcDir = File("${project.projectDir}/src/main/java/com/Serviseyem")
        val destSrcDir = File(backupDir, "src")
        srcDir.copyRecursively(destSrcDir, overwrite = true)
        
        // Copy build files
        File("${project.projectDir}/build.gradle.kts").copyTo(File(backupDir, "app_build.gradle.kts"), overwrite = true)
        File("${project.rootDir}/build.gradle.kts").copyTo(File(backupDir, "root_build.gradle.kts"), overwrite = true)
        File("${project.rootDir}/settings.gradle.kts").copyTo(File(backupDir, "settings.gradle.kts"), overwrite = true)
        
        println("Backup created successfully at ${backupDir.absolutePath}")
    }
}

