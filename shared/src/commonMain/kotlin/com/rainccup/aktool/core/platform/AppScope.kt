package com.rainccup.aktool.core.platform

import kotlinx.coroutines.CoroutineScope

/**
 * 应用级协程作用域：活到进程结束，不随页面销毁而取消。
 *
 * 用于"用户点保存后立刻返回上一页"这类必须跑完的操作——`viewModelScope` 会随页面一起取消，
 * 请求会半路夭折，并抛出 `CancellationException`（界面上表现为 "Job was cancelled"）。
 */
class AppScope(scope: CoroutineScope) : CoroutineScope by scope
