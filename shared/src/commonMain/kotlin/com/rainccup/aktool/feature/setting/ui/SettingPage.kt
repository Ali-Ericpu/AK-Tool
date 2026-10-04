package com.rainccup.aktool.feature.setting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rainccup.aktool.core.common.LocalAppConfig
import com.rainccup.aktool.core.common.OnConfigChange
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.designsystem.component.EditText
import com.rainccup.aktool.core.designsystem.theme.UNSET_PRIMARY_COLOR
import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.platform.FilePicker
import com.rainccup.aktool.feature.setting.viewmodel.SettingViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.admin_key
import com.rainccup.aktool.resources.choose_bg
import com.rainccup.aktool.resources.custom_bg
import com.rainccup.aktool.resources.dark_mode
import com.rainccup.aktool.resources.dynamic_color
import com.rainccup.aktool.resources.dynamic_color_summary
import com.rainccup.aktool.resources.primary_color
import com.rainccup.aktool.resources.primary_color_direct
import com.rainccup.aktool.resources.primary_color_follow_system
import com.rainccup.aktool.resources.reset_to_default
import com.rainccup.aktool.resources.server_uri
import com.rainccup.aktool.resources.uid
import com.rainccup.aktool.resources.update_excel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingPage() {
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = koinViewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsStateWithLifecycle()
    val filePicker: FilePicker = koinInject()
    val autofillManager = LocalAutofillManager.current
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AK TOOL",
            fontSize = 42.sp,
            autoSize = TextAutoSize.StepBased(maxFontSize = 30.sp),
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 12.dp)
                .align(Alignment.Start)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.background.copy(alpha = 0.8f))
                ) {
                    EditText(
                        value = config.serverUri,
                        label = stringResource(Res.string.server_uri),
                        hide = true,
                        singleLine = true,
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
                    ArrowPreference(
                        title = stringResource(Res.string.update_excel),
                        onClick = { viewModel.updateExcel(config.serverUri) },
                        enabled = !isUpdateExcel,
                        endActions = {
                            if (isUpdateExcel) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(start = 8.dp),
                                    size = 20.dp,
                                )
                            }
                        }
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.background.copy(alpha = 0.8f))
                ) {
                    SwitchPreference(
                        title = stringResource(Res.string.dark_mode),
                        checked = config.darkMode,
                        onCheckedChange = { onConfigChange(config.copy(darkMode = it)) },
                    )
                    ChooseColorDialog(config, onConfigChange)
                    SwitchPreference(
                        title = stringResource(Res.string.custom_bg),
                        checked = config.customBg,
                        onCheckedChange = { onConfigChange(config.copy(customBg = it)) }
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.choose_bg),
                        onClick = {
                            filePicker.pickImage { path ->
                                path?.let {
                                    onConfigChange(
                                        config.copy(
                                            bgPath = it,
                                            customBg = true
                                        )
                                    )
                                }
                            }
                        }
                    )

                }
            }
        }
    }
}

@Composable
private fun ChooseColorDialog(config: AppConfig, onConfigChange: OnConfigChange) {
    var show by remember { mutableStateOf(false) }
    var color by remember(config) { mutableStateOf(config.primaryColor) }
    var dynamicColor by remember(config) { mutableStateOf(config.dynamicColor) }
    ArrowPreference(
        title = stringResource(Res.string.primary_color),
        summary = when {
            config.primaryColor == UNSET_PRIMARY_COLOR -> stringResource(Res.string.primary_color_follow_system)
            !config.dynamicColor -> stringResource(Res.string.primary_color_direct)
            else -> null
        },
        onClick = { show = true },
        endActions = {
            Spacer(
                modifier = Modifier
                    .background(color = MiuixTheme.colorScheme.primary, shape = CircleShape)
                    .size(28.dp)
            )
        }
    )
    BasicDialog(
        title = stringResource(Res.string.primary_color),
        show = show,
        neutralText = stringResource(Res.string.reset_to_default),
        // 「恢复默认」只清掉主题色
        onNeutral = {
            show = false
            onConfigChange(config.copy(primaryColor = UNSET_PRIMARY_COLOR))
        },
        // 取消只丢弃草稿，色块与开关读的都是 config，因此自动回到已保存的状态
        onCancel = { show = false },
        onConfirm = {
            show = false
            onConfigChange(
                config.copy(
                    primaryColor = color,
                    dynamicColor = dynamicColor,
                )
            )
        }
    ) {
        SwitchPreference(
            title = stringResource(Res.string.dynamic_color),
            summary = stringResource(Res.string.dynamic_color_summary),
            checked = dynamicColor,
            onCheckedChange = { dynamicColor = it }
        )
        ColorPicker(
            color = Color(color),
            onColorChanged = { color = it.value }
        )
    }
}
