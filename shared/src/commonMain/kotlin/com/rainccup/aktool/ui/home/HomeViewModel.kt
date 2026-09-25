package com.rainccup.aktool.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.model.SaveStatusRequest
import com.rainccup.aktool.core.model.Status
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val admin: AdminRepository,
    private val messenger: Messenger,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _status = MutableStateFlow(Status.placeholder())
    val status: StateFlow<Status> = _status.asStateFlow()

    private val _isSplash = MutableStateFlow(true)
    val isSplash: StateFlow<Boolean> = _isSplash.asStateFlow()

    fun refresh() = viewModelScope.launch {
        _isRefreshing.emit(true)
        try {
            val result = admin.syncStatus()
            val data = result.data ?: error(result.msg)
            _status.emit(data)
            _isSplash.emit(false)
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
        _isRefreshing.emit(false)
    }

    fun updateStatus(request: SaveStatusRequest) = viewModelScope.launch {
        try {
            val result = admin.saveStatus(request)
            if (result.status != 0) error(result.msg)
            messenger.showSuccess()
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
        refresh()
    }
}
