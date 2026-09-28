package com.rainccup.aktool.core.domain.usecase.status

import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.model.SaveStatusRequest

/**
 * 主页（HomeViewModel）的用例入口：拉取与保存玩家状态。
 *
 * 每个 ViewModel 恰好对应一个 use case 类，它的方法就是这个页面的全部业务动作。
 * 拉取的失败信息交给 ViewModel 决定提示方式，use case 不碰 UI。
 */
class HomeUseCase(private val admin: AdminRepository) {

    suspend fun loadStatus() = admin.syncStatus()

    suspend fun saveStatus(request: SaveStatusRequest) = admin.saveStatus(request)
}
