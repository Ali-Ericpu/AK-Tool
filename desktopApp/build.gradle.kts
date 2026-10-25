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

// 桌面端没有 BuildConfig：把 Gradle 的 version 展开进 version.properties，
// 运行时由 DesktopAppBuildConfig 读回来，保证版本号只有一个来源。
// 版本号在配置阶段取成 lambda 内的局部量，避免闭包捕获脚本对象（会破坏 configuration cache）。
tasks.processResources {
    val v = project.version.toString()
    filesMatching("version.properties") {
        expand("version" to v)
    }
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
