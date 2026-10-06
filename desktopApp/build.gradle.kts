import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.koin.core)
}

val desktopPackageVersion: String = (findProperty("aktool.versionName") as String?)
    ?.takeIf { it.isNotBlank() }
    ?: "2.0.0"

compose.desktop {
    application {
        mainClass = "com.rainccup.aktool.MainKt"
        nativeDistributions {
            packageVersion = desktopPackageVersion
            packageName = "AK Tool"
        }
    }
}
