package com.rainccup.aktool.ui.extra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.ApiResult
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

class ExtraViewModel(
    private val admin: AdminRepository,
    private val messenger: Messenger,
    private val clipboard: ClipboardPort,
) : ViewModel() {
    private val _showUnlockChar = MutableStateFlow(false)
    val showUnlockChar: StateFlow<Boolean> = _showUnlockChar.asStateFlow()

    private val _showMessageDialog = MutableStateFlow(false)
    val showMessageDialog: StateFlow<Boolean> = _showMessageDialog.asStateFlow()

    private val _showItemDialog = MutableStateFlow(false)
    val showItemDialog: StateFlow<Boolean> = _showItemDialog.asStateFlow()

    private val _showActivityDialog = MutableStateFlow(false)
    val showActivityDialog: StateFlow<Boolean> = _showActivityDialog.asStateFlow()

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    private val _showValidCodeDialog = MutableStateFlow(false)
    val showValidCodeDialog: StateFlow<Boolean> = _showValidCodeDialog.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private var validateCode = mapOf<String, String>()

    fun changeUnlockCharState() = viewModelScope.launch {
        _showUnlockChar.emit(_showUnlockChar.value.not())
    }

    fun changeMessageState() = viewModelScope.launch {
        _showMessageDialog.emit(_showMessageDialog.value.not())
    }

    fun changeItemState() = viewModelScope.launch {
        _showItemDialog.emit(_showItemDialog.value.not())
    }

    fun changeActivityState() = viewModelScope.launch {
        _showActivityDialog.emit(_showActivityDialog.value.not())
    }

    fun changeAccountState() = viewModelScope.launch {
        _showAccountDialog.emit(_showAccountDialog.value.not())
    }

    fun changeValidCodeState() = viewModelScope.launch {
        _showValidCodeDialog.emit(_showValidCodeDialog.value.not())
    }

    private suspend fun doRequest(request: suspend () -> ApiResult<JsonElement?>) {
        _isConnecting.emit(true)
        try {
            val result = request()
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
            messenger.showSuccess()
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
        _isConnecting.emit(false)
    }

    fun unlockAllChar(body: UnlockAllCharRequest) = viewModelScope.launch {
        doRequest { admin.unlockAllChar(body) }
    }

    fun unlockAllStages() = viewModelScope.launch {
        doRequest { admin.unlockAllStages() }
    }

    fun unlockAllFlags() = viewModelScope.launch {
        doRequest { admin.unlockAllFlags() }
    }

    fun addFlushMessage(body: AddFlushMessageRequest) = viewModelScope.launch {
        doRequest { admin.addFlushMessage(body) }
    }

    fun gainItem(body: Item) = viewModelScope.launch {
        doRequest { admin.gainItem(GainItemRequest(listOf(body))) }
    }

    fun resetActivity(body: ResetActivityRequest) = viewModelScope.launch {
        doRequest { admin.resetActivity(body) }
    }

    fun registerAccount(body: RegisterAccountRequest) = viewModelScope.launch {
        doRequest { admin.registerAccount(body) }
    }

    fun resetRlv2() = viewModelScope.launch {
        doRequest { admin.resetRlv2() }
    }

    fun syncValidCode() = viewModelScope.launch {
        _isConnecting.emit(true)
        runCatching {
            val result = admin.syncValidCode()
            validateCode = result.data ?: throw RuntimeException(result.msg)
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
        _isConnecting.emit(false)
    }

    fun validateCodeList(): List<Pair<String, String>> = validateCode.toList()

    fun queryAccountByUID() = viewModelScope.launch {
        runCatching {
            val account = admin.queryAccountByUID().data!!["account"]!!
            clipboard.setText(account)
            messenger.show("复制成功 : $account")
        }.onFailure {
            messenger.show(it.message ?: "error")
        }
    }
}
