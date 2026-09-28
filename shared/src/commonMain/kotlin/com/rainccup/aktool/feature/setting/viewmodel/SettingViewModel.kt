package com.rainccup.aktool.feature.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.domain.usecase.config.SettingUseCase
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.error_uri
import com.rainccup.aktool.resources.update_fail
import com.rainccup.aktool.resources.update_success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class SettingViewModel(
    private val useCase: SettingUseCase,
    private val messenger: Messenger,
) : ViewModel() {
    private val _isUpdateExcel = MutableStateFlow(false)
    val isUpdateExcel: StateFlow<Boolean> = _isUpdateExcel.asStateFlow()

    fun updateExcel(uri: String) {
        if (useCase.showConfigError()) {
            viewModelScope.launch { messenger.show(getString(Res.string.error_uri)) }
            return
        }
        viewModelScope.launch {
            _isUpdateExcel.emit(true)
            runCatching { useCase.updateGameTable(uri) }.onSuccess { updated ->
                if (updated) messenger.show(getString(Res.string.update_success))
            }.onFailure {
                Logger.d { "updateExcelFail: ${it.message}" }
                messenger.show(getString(Res.string.update_fail))
            }
            _isUpdateExcel.emit(false)
        }
    }
}
