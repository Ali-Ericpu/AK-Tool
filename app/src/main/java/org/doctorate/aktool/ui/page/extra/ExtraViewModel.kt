package org.doctorate.aktool.ui.page.extra

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.doctorate.aktool.R
import org.doctorate.aktool.network.Network
import org.doctorate.aktool.pojo.entity.Item
import org.doctorate.aktool.pojo.entity.Result
import org.doctorate.aktool.pojo.request.AddFlushMessageRequest
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.UnlockAllCharRequest

class ExtraViewModel : ViewModel() {

    private var _showUnlockChar = MutableStateFlow(false)
    val showUnlockChar = _showUnlockChar.asStateFlow()

    private var _showMessageDialog = MutableStateFlow(false)
    val showMessageDialog = _showMessageDialog.asStateFlow()

    private var _showItemDialog = MutableStateFlow(false)
    val showItemDialog = _showItemDialog.asStateFlow()

    private var _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    fun changeUnlockCharState() = viewModelScope.launch {
        _showUnlockChar.emit(_showUnlockChar.value.not())
    }

    fun changeMessageState() = viewModelScope.launch {
        _showMessageDialog.emit(_showMessageDialog.value.not())
    }

    fun changeItemState() = viewModelScope.launch {
        _showItemDialog.emit(_showItemDialog.value.not())
    }

    private suspend fun doRequest(
        context: Context,
        request: suspend () -> Result<Map<String, Any>?>
    ) {
        _isConnecting.emit(true)
        try {
            val result = request()
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
            Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        _isConnecting.emit(false)
    }

    fun unlockAllChar(body: UnlockAllCharRequest, context: Context) = viewModelScope.launch {
        doRequest(context) {
            Network.unlockAllChar(body)
        }
    }

    fun unlockAllStages(context: Context) = viewModelScope.launch {
        doRequest(context) {
            Network.unlockAllStages()
        }
    }

    fun unlockAllFlags(context: Context) = viewModelScope.launch {
        doRequest(context) {
            Network.unlockAllFlags()
        }
    }

    fun addFlushMessage(body: AddFlushMessageRequest, context: Context) = viewModelScope.launch {
        doRequest(context) {
            Network.addFlushMessage(body)
        }
    }

    fun gainItem(body: Item, context: Context) = viewModelScope.launch {
        doRequest(context) {
            Network.gainItem(GainItemRequest(listOf(body)))
        }
    }

}