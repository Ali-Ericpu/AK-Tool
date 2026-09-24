package com.rainccup.aktool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.rainccup.aktool.core.platform.AndroidContextHolder
import com.rainccup.aktool.core.platform.AndroidFilePickerBridge
import com.rainccup.aktool.di.appModule
import com.rainccup.aktool.di.viewModelModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextHolder.appContext = applicationContext
        if (GlobalContext.getOrNull() == null) {
            startKoin { modules(appModule, viewModelModule) }
        }
        setContent {
            val pickImage = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri -> pendingImage?.invoke(uri?.toString()); pendingImage = null }
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
