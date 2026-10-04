package com.rainccup.aktool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.rainccup.aktool.app.App
import com.rainccup.aktool.app.appModule
import com.rainccup.aktool.core.platform.AndroidContextHolder
import com.rainccup.aktool.core.platform.AndroidFilePickerBridge
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.io.File
import kotlin.uuid.Uuid

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
                uri?.let {
                    applicationContext.contentResolver.openInputStream(uri).use { input ->
                        input?.let {
                            val directory = File(applicationContext.filesDir, "data/background/")
                            directory.deleteRecursively()
                            directory.mkdirs()
                            val file = File(directory, "${Uuid.random()}.0")
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                            pendingImage?.invoke(file.path)
                        }
                    }
                }
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

    companion object {
        private var pendingImage: ((String?) -> Unit)? = null
        private var pendingFile: ((String?) -> Unit)? = null
    }
}
