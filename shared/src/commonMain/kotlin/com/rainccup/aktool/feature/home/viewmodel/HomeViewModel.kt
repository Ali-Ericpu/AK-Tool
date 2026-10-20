package com.rainccup.aktool.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.status.HomeUseCase
import com.rainccup.aktool.core.model.SaveStatusRequest
import com.rainccup.aktool.core.model.Status
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.save_success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(
    private val useCase: HomeUseCase,
    private val messenger: Messenger,
) : ViewModel() {
    val isRefreshing: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val status: StateFlow<Status>
        field = MutableStateFlow(Status.placeholder())

    val isSplash: StateFlow<Boolean>
        field = MutableStateFlow(true)

    fun refresh() = viewModelScope.launch {
        isRefreshing.value = true
        try {
            val result = useCase.loadStatus()
            val data = result.data ?: error(result.msg)
            status.value = data
            isSplash.value = false

        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        }
        delay(500.milliseconds)
        isRefreshing.value = false
    }

    fun updateStatus(request: SaveStatusRequest) = viewModelScope.launch {
        try {
            val result = useCase.saveStatus(request)
            if (result.status != 0) error(result.msg)
            messenger.show(getString(Res.string.save_success))
        } catch (e: Exception) {
            messenger.show(e.message ?: "error")
        } finally {
            refresh()
        }
    }
}
