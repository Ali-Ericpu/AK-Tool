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
import com.rainccup.aktool.resources.save_success
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import org.jetbrains.compose.resources.getString

class ExtraViewModel(
    private val useCase: ExtraUseCase,
    private val messenger: Messenger,
    private val clipboard: ClipboardPort,
) : ViewModel() {

    private suspend fun doRequest(request: suspend () -> ApiResult<JsonElement?>) {
        try {
            val result = request()
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
            messenger.show(getString(Res.string.save_success))
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
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

    fun resetAccount() = viewModelScope.launch {
        doRequest { useCase.resetAccount() }
    }

    fun resetRlv2() = viewModelScope.launch {
        doRequest { useCase.resetIntegratedStrategies() }
    }

    suspend fun syncValidCode(): Map<String, String> {
        return runCatching { useCase.syncValidCode() }.onFailure {
            messenger.show(it.message ?: "error")
        }.getOrElse { emptyMap() }
    }

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
