package com.rainccup.aktool.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.datastore.ConfigRepository
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val gameTable: GameTableRepository,
    private val configRepository: ConfigRepository,
    private val messenger: Messenger,
    private val httpClientProvider: HttpClientProvider,
) : ViewModel() {
    private val _isUpdateExcel = MutableStateFlow(false)
    val isUpdateExcel: StateFlow<Boolean> = _isUpdateExcel.asStateFlow()

    fun saveConfig(serverUri: String, uid: String, adminKey: String) {
        val cfg = configRepository.read()
        configRepository.write(
            cfg.copy(serverUri = serverUri, uid = uid, adminKey = adminKey)
        )
        httpClientProvider.recreate(serverUri.ifBlank { cfg.serverUri })
        messenger.showSuccess()
    }

    fun updateExcel(uri: String) {
        viewModelScope.launch {
            _isUpdateExcel.emit(true)
            runCatching { gameTable.refresh(uri) }.onSuccess {
                messenger.show("更新成功")
            }.onFailure {
                Logger.d { "updateExcelFail: ${it.message}" }
                messenger.show("更新失败")
            }
            _isUpdateExcel.emit(false)
        }
    }
}
