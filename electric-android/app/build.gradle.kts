import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val versionMajor = project.property("APP_VERSION_MAJOR").toString().toInt()
val versionMinor = project.property("APP_VERSION_MINOR").toString().toInt()

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.github.fields.electric"

    defaultConfig {
        applicationId = "com.github.fields.electric"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = (versionMajor * 100) + versionMinor
        versionName = "${versionMajor}.${versionMinor}"

        // Save each rendered frame to file?
        buildConfigField("boolean", "SAVE_FRAMES", "false")
    }

    signingConfigs {
        create("release") {
            val releaseFilePath = project.properties["RELEASE_STORE_FILE"] as String
            if (!releaseFilePath.isEmpty()) {
                storeFile = file(releaseFilePath)
                storePassword = project.properties["RELEASE_STORE_PASSWORD"] as String
                keyAlias = project.properties["RELEASE_KEY_ALIAS"] as String
                keyPassword = project.properties["RELEASE_KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    lint {
        disable += "AllowBackup"
        disable += "AlwaysShowAction"
        disable += "ClickableViewAccessibility"
        disable += "GoogleAppIndexingWarning"
    }
}

dependencies {
    // Jetpack
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)

    // Logging
    implementation(libs.timber)

    // TODO migrate Rx to Flow
    implementation(libs.rxandroid)

    // Testing
    testImplementation(libs.junit)
}
