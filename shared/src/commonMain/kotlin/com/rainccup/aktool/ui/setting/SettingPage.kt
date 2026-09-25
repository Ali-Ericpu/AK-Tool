package com.rainccup.aktool.ui.setting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.rainccup.aktool.config.LocalAppConfig
import com.rainccup.aktool.core.designsystem.component.ActionButton
import com.rainccup.aktool.core.designsystem.component.EditSwitch
import com.rainccup.aktool.core.designsystem.component.EditText
import com.rainccup.aktool.core.designsystem.component.ProgressActionButton
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.admin_key
import com.rainccup.aktool.resources.choose_bg
import com.rainccup.aktool.resources.custom_bg
import com.rainccup.aktool.resources.dark_mode
import com.rainccup.aktool.resources.server_uri
import com.rainccup.aktool.resources.uid
import com.rainccup.aktool.resources.update_excel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingPage() {
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = koinViewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val filePicker: com.rainccup.aktool.core.platform.FilePicker = koinInject()
    val httpClientProvider: HttpClientProvider = koinInject()
    LazyColumn(modifier = Modifier.alpha(0.9f)) {
        item {
            EditText(
                value = config.serverUri,
                label = stringResource(Res.string.server_uri),
                hide = true,
                onValueSave = {
                    if (it.isNotBlank()) {
                        httpClientProvider.recreate(it)
                        onConfigChange(config.copy(serverUri = it))
                    }
                }
            )
            EditText(
                value = config.uid,
                label = stringResource(Res.string.uid),
                onValueSave = { onConfigChange(config.copy(uid = it)) }
            )
            EditText(
                value = config.adminKey,
                label = stringResource(Res.string.admin_key),
                hide = true,
                onValueSave = { onConfigChange(config.copy(adminKey = it)) }
            )
            EditSwitch(
                label = stringResource(Res.string.dark_mode),
                state = config.darkMode,
                onCheckedChange = { onConfigChange(config.copy(darkMode = it)) }
            )
            EditSwitch(
                label = stringResource(Res.string.custom_bg),
                state = config.customBg,
                onCheckedChange = { onConfigChange(config.copy(customBg = it)) }
            )
            ActionButton(label = stringResource(Res.string.choose_bg)) {
                filePicker.pickImage { path -> path?.let { onConfigChange(config.copy(bgPath = it)) } }
            }
            ProgressActionButton(
                label = stringResource(Res.string.update_excel),
                isUpdate = isUpdateExcel,
                onClick = {
                    if (config.serverUri.isEmpty()) {
                        
                    } else {
                        coroutineScope.launch { viewModel.updateExcel(config.serverUri) }
                    }
                }
            )
        }
    }
}
