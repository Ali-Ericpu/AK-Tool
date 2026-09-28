package com.rainccup.aktool.core.domain

import com.rainccup.aktool.core.domain.usecase.admin.ExtraUseCase
import com.rainccup.aktool.core.domain.usecase.character.CharacterDetailUseCase
import com.rainccup.aktool.core.domain.usecase.character.CharacterUseCase
import com.rainccup.aktool.core.domain.usecase.config.SettingUseCase
import com.rainccup.aktool.core.domain.usecase.status.HomeUseCase
import org.koin.dsl.module

/**
 * Use cases 无状态，一律 factory。
 *
 * 每个 ViewModel 恰好注入一个 use case——它就是这个页面的业务动作全集，
 * 类名与页面一一对应（Home / Character / CharacterDetail / Extra / Setting）。
 */
val domainModule = module {
    factory { HomeUseCase(get()) }
    factory { CharacterUseCase(get(), get()) }
    factory { CharacterDetailUseCase(get(), get()) }
    factory { ExtraUseCase(get(), get()) }
    factory { SettingUseCase(get(), get()) }

    // 只读表查询门面：
    single { GameTableQuery(get()) }
}
