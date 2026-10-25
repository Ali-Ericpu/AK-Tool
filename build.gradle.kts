plugins {
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
}

// 版本号的唯一来源：gradle/libs.versions.toml 的 app-version。
//
// 这里查 catalog 而不是在 allprojects{} 里写 `libs.versions.app.version.get()`：
// 那个访问器在 allprojects 闭包里对部分子工程不可用，只能在各模块自己的脚本里用。
val appVersion = extensions.getByType<VersionCatalogsExtension>()
    .named("libs")
    .findVersion("app-version")
    .get()
    .requiredVersion
check(appVersion != "unspecified") { "app-version is missing from gradle/libs.versions.toml" }

allprojects {
    // versionName（Android）、packageVersion（桌面）与 version.properties 都从这里派生
    version = appVersion
}
