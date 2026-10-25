package com.rainccup.aktool.feature.setting.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rainccup.aktool.core.common.AppInfo
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.update.UpdateUiState
import com.rainccup.aktool.feature.setting.viewmodel.SettingViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.update_available_title
import com.rainccup.aktool.resources.update_check
import com.rainccup.aktool.resources.update_current_version
import com.rainccup.aktool.resources.update_download_and_install
import com.rainccup.aktool.resources.update_downloading
import com.rainccup.aktool.resources.update_failed_title
import com.rainccup.aktool.resources.update_install_now
import com.rainccup.aktool.resources.update_installing
import com.rainccup.aktool.resources.update_later
import com.rainccup.aktool.resources.update_need_permission_summary
import com.rainccup.aktool.resources.update_need_permission_title
import com.rainccup.aktool.resources.update_open_settings
import com.rainccup.aktool.resources.update_retry
import com.rainccup.aktool.resources.update_up_to_date
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 设置页里的「检查更新」入口 + 更新流程弹窗。
 *
 * 下载进度直接显示在入口行的摘要里，不弹窗——弹窗没有按钮可点，反而挡视线。
 */
@Composable
fun UpdateSection(viewModel: SettingViewModel) {
    val appInfo: AppInfo = koinInject()
    val state by viewModel.updateState.collectAsStateWithLifecycle()
    val downloading = state as? UpdateUiState.Downloading

    // 用户去系统设置里开「安装未知应用」，回到前台时接着把已下载的包装掉。
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.resumeUpdateAfterPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ArrowPreference(
        title = stringResource(Res.string.update_check),
        summary = when {
            downloading != null ->
                stringResource(Res.string.update_downloading, (downloading.progress * 100).toInt())

            state is UpdateUiState.Checking -> stringResource(Res.string.update_check)
            state is UpdateUiState.UpToDate ->
                stringResource(
                    Res.string.update_up_to_date,
                    (state as UpdateUiState.UpToDate).version
                )

            else -> stringResource(Res.string.update_current_version, appInfo.versionName)
        },
        onClick = { if (state !is UpdateUiState.Available) viewModel.checkUpdate() },
        enabled = state !is UpdateUiState.Checking && downloading == null,
        endActions = {
            if (state is UpdateUiState.Checking || downloading != null) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 8.dp),
                    size = 20.dp,
                )
            }
        },
    )

    when (val current = state) {
        is UpdateUiState.Available -> BasicDialog(
            title = stringResource(Res.string.update_available_title, current.version),
            show = true,
            cancelText = stringResource(Res.string.update_later),
            confirmText = stringResource(Res.string.update_download_and_install),
            onCancel = { viewModel.dismissUpdate() },
            onConfirm = { viewModel.downloadUpdate() },
        ) {
            if (current.notes.isNotBlank()) {
                Text(
                    text = current.notes,
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }

        is UpdateUiState.Ready -> BasicDialog(
            title = stringResource(Res.string.update_available_title, current.version),
            show = true,
            summary = stringResource(Res.string.update_installing),
            cancelText = stringResource(Res.string.update_later),
            confirmText = stringResource(Res.string.update_install_now),
            onCancel = { viewModel.dismissUpdate() },
            onConfirm = { viewModel.installUpdate() },
        )

        is UpdateUiState.NeedPermission -> BasicDialog(
            title = stringResource(Res.string.update_need_permission_title),
            show = true,
            summary = stringResource(Res.string.update_need_permission_summary),
            cancelText = stringResource(Res.string.update_later),
            confirmText = stringResource(Res.string.update_open_settings),
            onCancel = { viewModel.dismissUpdate() },
            // 不重置状态：保留已下载的路径，授权回来后 ON_RESUME 会接着安装
            onConfirm = { viewModel.openInstallPermission() },
        )

        is UpdateUiState.Failed -> BasicDialog(
            title = stringResource(Res.string.update_failed_title),
            show = true,
            summary = current.message,
            cancelText = stringResource(Res.string.update_later),
            confirmText = stringResource(Res.string.update_retry),
            onCancel = { viewModel.dismissUpdate() },
            onConfirm = {
                viewModel.dismissUpdate()
                viewModel.checkUpdate()
            },
        )

        else -> Unit
    }
}
