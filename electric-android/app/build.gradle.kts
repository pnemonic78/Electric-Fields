plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

val versionMajor = (project.property("APP_VERSION_MAJOR") as String).toInt()
val versionMinor = (project.property("APP_VERSION_MINOR") as String).toInt()

android {
    compileSdkVersion(Extra.androidBuildSdkVersion)

    defaultConfig {
        applicationId = "com.github.fields.electric"
        minSdkVersion(Extra.androidBuildMinSdkVersion)
        targetSdkVersion(Extra.androidBuildTargetSdkVersion)
        versionCode = versionMajor * 100 + versionMinor
        versionName = "${versionMajor}.${versionMinor.toString().padLeft(2, "0")}"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = project.property("STORE_PASSWORD_RELEASE") as String
            keyAlias = "release"
            keyPassword = project.property("KEY_PASSWORD_RELEASE") as String
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        disable("AllowBackup")
        disable("AlwaysShowAction")
        disable("ClickableViewAccessibility")
        disable("GoogleAppIndexingWarning")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Extra.kotlinVersion}")

    // Rx
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    // AndroidX
    implementation("androidx.core:core:1.3.1")

    // Testing
    testImplementation("junit:junit:4.13")
}
