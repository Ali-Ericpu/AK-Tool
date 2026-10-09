import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

android {
    namespace = "com.rainccup.aktool"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rainccup.aktool"
        minSdk = 28
        targetSdk = 37
        versionCode = 201
        versionName = project.version.toString()
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

androidComponents {
    val gitHash = providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.getOrElse("nogit").trim()
    val format = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
        .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("${rootProject.name}_${variant.name}_v${version}_${format}_${gitHash}.apk")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.core)
}
