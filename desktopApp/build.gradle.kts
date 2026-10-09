import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.koin.core)
}

compose.desktop {
    application {
        mainClass = "com.rainccup.aktool.MainKt"
        nativeDistributions {
            packageVersion = version.toString()
            packageName = "AK Tool"
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            licenseFile.set(rootProject.file("LICENSE"))
            windows {
                iconFile.set(rootProject.file("assets/app-icon/icon.ico"))
            }
        }
    }
}
