import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

/*
 * Release signing is supplied through the environment so that CI can sign without a keystore
 * ever being committed. Export these four variables (see .github/workflows/build.yml):
 *
 *   AKTOOL_KEYSTORE_FILE      path to the .jks / .keystore
 *   AKTOOL_KEYSTORE_PASSWORD  keystore password
 *   AKTOOL_KEY_ALIAS          key alias
 *   AKTOOL_KEY_PASSWORD       key password
 *
 * When any of them is missing the release build stays unsigned, which is what a plain
 * `assembleRelease` has always produced in this repository.
 */
val releaseKeystore = System.getenv("AKTOOL_KEYSTORE_FILE")
    ?.takeIf { it.isNotBlank() }
    ?.let { file(it) }
    ?.takeIf { it.exists() }
val releaseKeystorePassword = System.getenv("AKTOOL_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("AKTOOL_KEY_ALIAS")
val releaseKeyPassword = System.getenv("AKTOOL_KEY_PASSWORD")
val hasReleaseSigning = releaseKeystore != null &&
    !releaseKeystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

/*
 * Version is overridable from the command line so a release can be built straight from a tag
 * (see .github/workflows/build.yml):
 *
 *   ./gradlew :androidApp:assembleRelease -Paktool.versionName=x.x.x -Paktool.versionCode=xx
 *
 * Without those properties the values below are used. `versionCode` must keep increasing, or
 * Android refuses to install the new build over an older one.
 */
val appVersionName: String = (findProperty("aktool.versionName") as String?)
    ?.takeIf { it.isNotBlank() }
    ?: "2.0.0"
val appVersionCode: Int = (findProperty("aktool.versionCode") as String?)
    ?.toIntOrNull()
    ?: 100

android {
    namespace = "com.rainccup.aktool"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rainccup.aktool"
        minSdk = 28
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.core)
}
