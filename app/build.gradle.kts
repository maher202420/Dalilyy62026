import java.io.FileInputStream
import java.util.Properties
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.Serviseyem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.Serviseyem"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Load settings from environment or .env file
        var geminiKey = System.getenv("GEMINI_API_KEY") ?: ""
        var adminUser = System.getenv("ADMIN_USERNAME") ?: ""
        var adminPass = System.getenv("ADMIN_PASSWORD") ?: ""
        var ownerPass = System.getenv("OWNER_PASSWORD") ?: ""

        val envFile = project.rootProject.file(".env").let { if (it.exists()) it else project.file(".env") }
        if (envFile.exists()) {
            val envProperties = Properties()
            FileInputStream(envFile).use { envProperties.load(it) }
            if (geminiKey.isEmpty()) geminiKey = envProperties.getProperty("GEMINI_API_KEY") ?: ""
            if (adminUser.isEmpty()) adminUser = envProperties.getProperty("ADMIN_USERNAME") ?: ""
            if (adminPass.isEmpty()) adminPass = envProperties.getProperty("ADMIN_PASSWORD") ?: ""
            if (ownerPass.isEmpty()) ownerPass = envProperties.getProperty("OWNER_PASSWORD") ?: ""
        }

        // Defaults if still empty
        if (adminUser.isEmpty()) adminUser = "WAM2026"
        if (adminPass.isEmpty()) adminPass = "maher736462"
        if (ownerPass.isEmpty()) ownerPass = "maher--736462"

        fun getSha256Hex(input: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { byte -> String.format("%02x", byte) }
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "ADMIN_USERNAME", "\"$adminUser\"")
        buildConfigField("String", "ADMIN_PASSWORD_HASH", "\"${getSha256Hex(adminPass)}\"")
        buildConfigField("String", "OWNER_PASSWORD_HASH", "\"${getSha256Hex(ownerPass)}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // matches Kotlin 1.9.24
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Testing
    testImplementation("junit:junit:4.13.2")
}
