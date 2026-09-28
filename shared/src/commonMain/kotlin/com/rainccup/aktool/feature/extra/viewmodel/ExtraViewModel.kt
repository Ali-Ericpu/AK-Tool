package com.rainccup.aktool.feature.extra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.admin.ExtraUseCase
import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.copy_success
import com.rainccup.aktool.resources.game_table_init_fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import org.jetbrains.compose.resources.getString

class ExtraViewModel(
    private val useCase: ExtraUseCase,
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
        if (showUnlockChar.value || useCase.init()) {
            showUnlockChar.emit(!showUnlockChar.value)
        } else {
            messenger.show(getString(Res.string.game_table_init_fail))
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
        doRequest { useCase.unlockAllCharacters(body) }
    }

    fun unlockAllStages() = viewModelScope.launch {
        doRequest { useCase.unlockAllStages() }
    }

    fun unlockAllFlags() = viewModelScope.launch {
        doRequest { useCase.unlockAllFlags() }
    }

    fun addFlushMessage(body: AddFlushMessageRequest) = viewModelScope.launch {
        doRequest { useCase.addFlushMessage(body) }
    }

    fun gainItem(body: Item) = viewModelScope.launch {
        doRequest { useCase.gainItem(body) }
    }

    fun resetActivity(body: ResetActivityRequest) = viewModelScope.launch {
        doRequest { useCase.resetActivity(body) }
    }

    fun registerAccount(body: RegisterAccountRequest) = viewModelScope.launch {
        doRequest { useCase.registerAccount(body) }
    }

    fun resetRlv2() = viewModelScope.launch {
        doRequest { useCase.resetIntegratedStrategies() }
    }

    fun syncValidCode() = viewModelScope.launch {
        isConnecting.emit(true)
        runCatching {
            validateCode = useCase.syncValidCode()
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
        isConnecting.emit(false)
    }

    fun validateCodeList(): List<Pair<String, String>> = validateCode.toList()

    fun queryAccountByUID() = viewModelScope.launch {
        runCatching {
            val account = useCase.queryAccount()
            clipboard.setText(account)
            messenger.show(getString(Res.string.copy_success, account))
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
    }
}
