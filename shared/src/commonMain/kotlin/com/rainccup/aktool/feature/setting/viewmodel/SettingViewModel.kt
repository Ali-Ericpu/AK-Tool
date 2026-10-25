package com.rainccup.aktool.feature.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.domain.usecase.config.SettingUseCase
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.update.UpdateService
import com.rainccup.aktool.core.update.UpdateUiState
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.error_uri
import com.rainccup.aktool.resources.update_fail
import com.rainccup.aktool.resources.update_success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class SettingViewModel(
    private val useCase: SettingUseCase,
    private val messenger: Messenger,
    private val updateService: UpdateService,
) : ViewModel() {
    val isUpdateExcel: StateFlow<Boolean>
        field = MutableStateFlow(false)

    /** 更新状态挂在应用级单例上，这里只做转发，页面销毁不会打断下载。 */
    val updateState: StateFlow<UpdateUiState> = updateService.state

    fun updateExcel(uri: String) {
        viewModelScope.launch {
            if (useCase.showConfigError()) {
                messenger.show(getString(Res.string.error_uri))
                return@launch
            }
            isUpdateExcel.emit(true)
            runCatching { useCase.updateGameTable(uri) }.onSuccess { updated ->
                if (updated) messenger.show(getString(Res.string.update_success))
            }.onFailure {
                Logger.d { "updateExcelFail: ${it.message}" }
                messenger.show(getString(Res.string.update_fail))
            }
            isUpdateExcel.emit(false)
        }
    }

    fun checkUpdate() = updateService.checkInBackground()

    fun changeUpdateDialogState() = updateService.changeUpdateDialogState()

    fun downloadUpdate() = updateService.downloadInBackground()

    fun installUpdate() = updateService.installInBackground()

    fun dismissUpdate() = updateService.dismiss()

    fun openInstallPermission() = updateService.requestInstallPermission()

    fun resumeUpdateAfterPermission() = updateService.installIfPermitted()
}
