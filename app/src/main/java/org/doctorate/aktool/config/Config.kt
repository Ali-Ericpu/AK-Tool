package org.doctorate.aktool.config

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.compositionLocalOf
import org.doctorate.aktool.utils.JsonUtil
import java.io.File

data class AppConfig(
    val serverUri: String = "",
    val darkMode: Boolean = false,
    val dynamicColor: Boolean = true,
    val uid: String = "",
    val adminKey: String = "",
    val bgPicUri: String = "",
) {
    companion object {
        var config = AppConfig()
    }
}

typealias OnConfigChange = (AppConfig) -> Unit

class AppConfigContext(val config: AppConfig = AppConfig(), val onConfigChange: OnConfigChange)

val LocalAppConfig = compositionLocalOf { AppConfigContext { } }

fun readConfig(context: Context): AppConfig {
    val configFile = File(context.filesDir, "config/config.json")
    Log.d("Load_Config", "readConfig: Success")
    return if (configFile.exists()) {
        JsonUtil.fromJson<AppConfig>(configFile).also { AppConfig.config = it }
    } else {
        AppConfig().also { config ->
            writeConfig(
                newConfig = config,
                context = context,
                onSuccess = { },
                onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() })
        }
    }
}

fun writeConfig(
    newConfig: AppConfig,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    AppConfig.config = newConfig
    try {
        JsonUtil.writeToFile(
            newConfig,
            File(context.filesDir, "config/config.json"),
            onSuccess,
            onFailure
        )
    } catch (e: Exception) {
        e.localizedMessage?.let { onFailure(it) }
        e.printStackTrace()
    }
}
