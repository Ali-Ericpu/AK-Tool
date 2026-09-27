package com.rainccup.aktool.feature.setting.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import com.rainccup.aktool.core.common.LocalAppConfig
import com.rainccup.aktool.core.designsystem.component.ActionButton
import com.rainccup.aktool.core.designsystem.component.EditText
import com.rainccup.aktool.core.designsystem.component.ProgressActionButton
import com.rainccup.aktool.core.platform.FilePicker
import com.rainccup.aktool.feature.setting.viewmodel.SettingViewModel
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
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SettingPage() {
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = koinViewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val filePicker: FilePicker = koinInject()
    val autofillManager = LocalAutofillManager.current
    LazyColumn {
        item {
            EditText(
                value = config.serverUri,
                label = stringResource(Res.string.server_uri),
                hide = true,
                singleLine = true,
                autoSize = TextAutoSize.StepBased(),
                onValueSave = {
                    if (it.isNotBlank()) {
                        onConfigChange(config.copy(serverUri = it))
                    }
                }
            )
            EditText(
                value = config.uid,
                label = stringResource(Res.string.uid),
                singleLine = true,
                onValueSave = { onConfigChange(config.copy(uid = it)) },
                modifier = Modifier.semantics {
                    contentType = ContentType.Username + ContentType.NewUsername
                }
            )
            EditText(
                value = config.adminKey,
                label = stringResource(Res.string.admin_key),
                hide = true,
                singleLine = true,
                onValueSave = {
                    autofillManager?.commit()
                    onConfigChange(config.copy(adminKey = it))
                },
                modifier = Modifier.semantics {
                    contentType = ContentType.Password + ContentType.NewPassword
                }
            )
            SwitchPreference(
                title = stringResource(Res.string.dark_mode),
                checked = config.darkMode,
                onCheckedChange = { onConfigChange(config.copy(darkMode = it)) }
            )
            SwitchPreference(
                title = stringResource(Res.string.custom_bg),
                checked = config.customBg,
                onCheckedChange = { onConfigChange(config.copy(customBg = it)) }
            )
            ActionButton(label = stringResource(Res.string.choose_bg)) {
                filePicker.pickImage { path -> path?.let { onConfigChange(config.copy(bgPath = it)) } }
            }
            ProgressActionButton(
                label = stringResource(Res.string.update_excel),
                isUpdate = isUpdateExcel,
                onClick = {
                    coroutineScope.launch { viewModel.updateExcel(config.serverUri) }
                }
            )
        }
    }
}
