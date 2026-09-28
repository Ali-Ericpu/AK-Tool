package com.rainccup.aktool.core.common

import androidx.compose.runtime.compositionLocalOf
import com.rainccup.aktool.core.model.ImageConfig

/**
 * 干员立绘 / 技能 / 模组图床的前缀配置，由 `App` 在根部统一提供，页面与卡片通过
 * `LocalImageConfig.current` 取用
 *
 * 默认值取 [ImageConfig.default]
 */
val LocalImageConfig = compositionLocalOf { ImageConfig.default }
