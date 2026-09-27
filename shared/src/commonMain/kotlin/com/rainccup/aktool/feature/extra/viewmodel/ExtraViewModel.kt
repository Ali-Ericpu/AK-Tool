package com.rainccup.aktool.feature.extra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.admin.AddFlushMessageUseCase
import com.rainccup.aktool.core.domain.usecase.admin.GainItemUseCase
import com.rainccup.aktool.core.domain.usecase.admin.QueryAccountByUidUseCase
import com.rainccup.aktool.core.domain.usecase.admin.RegisterAccountUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetActivityUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetIntegratedStrategiesUseCase
import com.rainccup.aktool.core.domain.usecase.admin.SyncValidCodeUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllCharactersUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllFlagsUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllStagesUseCase
import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

class ExtraViewModel(
    private val unlockAllCharactersUseCase: UnlockAllCharactersUseCase,
    private val unlockAllStagesUseCase: UnlockAllStagesUseCase,
    private val unlockAllFlagsUseCase: UnlockAllFlagsUseCase,
    private val addFlushMessageUseCase: AddFlushMessageUseCase,
    private val gainItemUseCase: GainItemUseCase,
    private val resetActivityUseCase: ResetActivityUseCase,
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resetIntegratedStrategiesUseCase: ResetIntegratedStrategiesUseCase,
    private val syncValidCodeUseCase: SyncValidCodeUseCase,
    private val queryAccountByUidUseCase: QueryAccountByUidUseCase,
    private val messenger: Messenger,
    private val clipboard: ClipboardPort,
) : ViewModel() {
    val showUnlockChar: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showMessageDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showItemDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showActivityDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showAccountDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showValidCodeDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isConnecting: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private var validateCode = mapOf<String, String>()

    fun changeUnlockCharState() = viewModelScope.launch {
        if (showUnlockChar.value || unlockAllCharactersUseCase.init()) {
            showUnlockChar.emit(!showUnlockChar.value)
        } else {
            messenger.show("数据缺失，请更新数据后再试")
        }
    }

    fun changeMessageState() = viewModelScope.launch {
        showMessageDialog.emit(!showMessageDialog.value)
    }

    fun changeItemState() = viewModelScope.launch {
        showItemDialog.emit(!showItemDialog.value)
    }

    fun changeActivityState() = viewModelScope.launch {
        showActivityDialog.emit(!showActivityDialog.value)
    }

    fun changeAccountState() = viewModelScope.launch {
        showAccountDialog.emit(!showAccountDialog.value)
    }

    fun changeValidCodeState() = viewModelScope.launch {
        showValidCodeDialog.emit(!showValidCodeDialog.value)
    }

    private suspend fun doRequest(request: suspend () -> ApiResult<JsonElement?>) {
        isConnecting.emit(true)
        try {
            val result = request()
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
            messenger.showSuccess()
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
        isConnecting.emit(false)
    }

    fun unlockAllChar(body: UnlockAllCharRequest) = viewModelScope.launch {
        doRequest { unlockAllCharactersUseCase(body) }
    }

    fun unlockAllStages() = viewModelScope.launch {
        doRequest { unlockAllStagesUseCase() }
    }

    fun unlockAllFlags() = viewModelScope.launch {
        doRequest { unlockAllFlagsUseCase() }
    }

    fun addFlushMessage(body: AddFlushMessageRequest) = viewModelScope.launch {
        doRequest { addFlushMessageUseCase(body) }
    }

    fun gainItem(body: Item) = viewModelScope.launch {
        doRequest { gainItemUseCase(body) }
    }

    fun resetActivity(body: ResetActivityRequest) = viewModelScope.launch {
        doRequest { resetActivityUseCase(body) }
    }

    fun registerAccount(body: RegisterAccountRequest) = viewModelScope.launch {
        doRequest { registerAccountUseCase(body) }
    }

    fun resetRlv2() = viewModelScope.launch {
        doRequest { resetIntegratedStrategiesUseCase() }
    }

    fun syncValidCode() = viewModelScope.launch {
        isConnecting.emit(true)
        runCatching {
            validateCode = syncValidCodeUseCase()
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
        isConnecting.emit(false)
    }

    fun validateCodeList(): List<Pair<String, String>> = validateCode.toList()

    fun queryAccountByUID() = viewModelScope.launch {
        runCatching {
            val account = queryAccountByUidUseCase()
            clipboard.setText(account)
            messenger.show("复制成功 : $account")
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
    }
}
