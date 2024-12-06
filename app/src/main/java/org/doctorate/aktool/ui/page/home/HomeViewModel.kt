package org.doctorate.aktool.ui.page.home

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.doctorate.aktool.R
import org.doctorate.aktool.network.Network
import org.doctorate.aktool.pojo.entity.Status
import org.doctorate.aktool.pojo.request.SaveStatusRequest

class HomeViewModel() : ViewModel() {
    private var _isSplash = MutableStateFlow(true)
    val isSplash = _isSplash.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var _status = MutableStateFlow<Status>(Status.status())
    val status: StateFlow<Status> = _status.asStateFlow()

    fun refresh(context: Context) = viewModelScope.launch {
        _isRefreshing.emit(true)
        try {
            val result = Network.syncStatus()
            if (result.data == null) {
                throw RuntimeException(result.msg)
            }
            _status.emit(result.data)
            _isSplash.emit(false)
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        _isRefreshing.emit(false)
    }

    fun updateStatus(saveStatusRequest: SaveStatusRequest, context: Context) =
        viewModelScope.launch {
            try {
                val result = Network.saveStatus(saveStatusRequest)
                if (result.status != 0) {
                    throw RuntimeException(result.msg)
                }
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            refresh(context)
        }

}