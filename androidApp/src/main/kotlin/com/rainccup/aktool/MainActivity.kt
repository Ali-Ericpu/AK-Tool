package com.rainccup.aktool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.rainccup.aktool.core.platform.AndroidContextHolder
import com.rainccup.aktool.core.platform.AndroidFilePickerBridge
import com.rainccup.aktool.app.App
import com.rainccup.aktool.app.appModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextHolder.appContext = applicationContext
        if (GlobalContext.getOrNull() == null) {
            startKoin { modules(appModule) }
        }
        setContent {
            val pickImage = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                // 自定义背景会把这个 Uri 写进 config.json 长期使用，而 SAF 默认只授予
                // 「本次会话」的读权限。不申请常驻权限的话，进程一重启 content:// 就打不开了
                // ——表现就是设置好的背景失效。这里把它转成持久化读权限。
                uri?.let { persistReadPermission(it) }
                pendingImage?.invoke(uri?.toString())
                pendingImage = null
            }
            val pickFile = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri -> pendingFile?.invoke(uri?.toString()); pendingFile = null }
            AndroidFilePickerBridge.pickImageImpl = { cb ->
                pendingImage = cb
                pickImage.launch(arrayOf("image/*"))
            }
            AndroidFilePickerBridge.pickFileImpl = { cb ->
                pendingFile = cb
                pickFile.launch(arrayOf("*/*"))
            }
            App()
        }
    }

    /**
     * 把 SAF 返回的 content:// 读取权限转成持久化授权，让它在进程重启后依然可读。
     *
     * 并非所有 provider 都支持持久化（此时会抛 SecurityException）；那种情况下退化成
     * 「仅本次会话有效」，不该因此崩溃，所以忽略异常。
     */
    private fun persistReadPermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    companion object {
        private var pendingImage: ((String?) -> Unit)? = null
        private var pendingFile: ((String?) -> Unit)? = null
    }
}
