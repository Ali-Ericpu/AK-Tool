package com.rainccup.aktool.app

import com.rainccup.aktool.core.data.dataModule
import com.rainccup.aktool.core.domain.domainModule
import com.rainccup.aktool.core.message.messageModule
import com.rainccup.aktool.core.platform.platformModule
import com.rainccup.aktool.feature.character.viewmodel.characterModule
import com.rainccup.aktool.feature.characterdetail.viewmodel.characterDetailModule
import com.rainccup.aktool.feature.extra.viewmodel.extraModule
import com.rainccup.aktool.feature.home.viewmodel.homeModule
import com.rainccup.aktool.feature.setting.viewmodel.settingModule
import com.rainccup.aktool.feature.splash.viewmodel.splashModule
import org.koin.dsl.module

/** 应用外壳：把各层就近声明的 Koin module 聚合起来，入口模块只需加载这一个。 */
val appModule = module {
    includes(
        dataModule,
        platformModule,
        messageModule,
        domainModule,
        homeModule,
        characterModule,
        characterDetailModule,
        extraModule,
        settingModule,
        splashModule,
    )
}
