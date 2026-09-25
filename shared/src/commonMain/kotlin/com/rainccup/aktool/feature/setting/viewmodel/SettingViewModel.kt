package com.rainccup.aktool.feature.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.domain.usecase.config.GetConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.SaveConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.UpdateGameTableUseCase
import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val getConfigUseCase: GetConfigUseCase,
    private val saveConfigUseCase: SaveConfigUseCase,
    private val updateGameTableUseCase: UpdateGameTableUseCase,
    private val messenger: Messenger,
) : ViewModel() {
    private val _isUpdateExcel = MutableStateFlow(false)
    val isUpdateExcel: StateFlow<Boolean> = _isUpdateExcel.asStateFlow()

    fun readConfig() = getConfigUseCase()

    fun saveConfig(serverUri: String, uid: String, adminKey: String) {
        saveConfigUseCase(serverUri, uid, adminKey)
        messenger.showSuccess()
    }

    fun updateExcel(uri: String) {
        viewModelScope.launch {
            _isUpdateExcel.emit(true)
            runCatching { updateGameTableUseCase(uri) }.onSuccess { updated ->
                if (updated) messenger.show("更新成功")
            }.onFailure {
                Logger.d { "updateExcelFail: ${it.message}" }
                messenger.show("更新失败")
            }
            _isUpdateExcel.emit(false)
        }
    }
}
