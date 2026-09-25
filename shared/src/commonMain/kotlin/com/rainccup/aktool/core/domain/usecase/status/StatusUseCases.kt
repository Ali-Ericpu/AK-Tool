package com.rainccup.aktool.core.domain.usecase.status

import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.SaveStatusRequest

/** 主页状态：拉取（失败信息交给 ViewModel）与保存。 */
class GetStatusUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke() = admin.syncStatus()
}

class SaveStatusUseCase(private val admin: AdminRepository) {
    suspend operator fun invoke(request: SaveStatusRequest) = admin.saveStatus(request)
}
