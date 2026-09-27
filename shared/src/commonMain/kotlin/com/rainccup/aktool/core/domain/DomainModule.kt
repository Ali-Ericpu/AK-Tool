package com.rainccup.aktool.core.domain

import com.rainccup.aktool.core.domain.usecase.admin.AddFlushMessageUseCase
import com.rainccup.aktool.core.domain.usecase.admin.GainItemUseCase
import com.rainccup.aktool.core.domain.usecase.admin.QueryAccountByUidUseCase
import com.rainccup.aktool.core.domain.usecase.admin.RegisterAccountUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetActivityUseCase
import com.rainccup.aktool.core.domain.usecase.admin.ResetIntegratedStrategiesUseCase
import com.rainccup.aktool.core.domain.usecase.admin.SyncValidCodeUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllCharactersUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllFlagsUseCase
import com.rainccup.aktool.core.domain.usecase.admin.UnlockAllStagesUseCase
import com.rainccup.aktool.core.domain.usecase.character.ChangeEvolvePhaseUseCase
import com.rainccup.aktool.core.domain.usecase.character.GainCharacterUseCase
import com.rainccup.aktool.core.domain.usecase.character.GetCharacterLimitsUseCase
import com.rainccup.aktool.core.domain.usecase.character.LoadCharactersUseCase
import com.rainccup.aktool.core.domain.usecase.character.SaveCharacterUseCase
import com.rainccup.aktool.core.domain.usecase.config.GetConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.SaveConfigUseCase
import com.rainccup.aktool.core.domain.usecase.config.UpdateGameTableUseCase
import com.rainccup.aktool.core.domain.usecase.status.GetStatusUseCase
import com.rainccup.aktool.core.domain.usecase.status.SaveStatusUseCase
import org.koin.dsl.module

/** Use cases 无状态，一律 factory。 */
val domainModule = module {
    factory { LoadCharactersUseCase(get(), get()) }
    factory { SaveCharacterUseCase(get(), get()) }
    factory { GainCharacterUseCase(get()) }
    factory { ChangeEvolvePhaseUseCase(get()) }
    factory { GetCharacterLimitsUseCase(get()) }

    factory { UnlockAllCharactersUseCase(get(), get()) }
    factory { UnlockAllStagesUseCase(get()) }
    factory { UnlockAllFlagsUseCase(get()) }
    factory { AddFlushMessageUseCase(get()) }
    factory { GainItemUseCase(get()) }
    factory { ResetActivityUseCase(get()) }
    factory { RegisterAccountUseCase(get()) }
    factory { ResetIntegratedStrategiesUseCase(get()) }
    factory { SyncValidCodeUseCase(get()) }
    factory { QueryAccountByUidUseCase(get()) }

    factory { GetStatusUseCase(get()) }
    factory { SaveStatusUseCase(get()) }

    factory { GetConfigUseCase(get()) }
    factory { SaveConfigUseCase(get()) }
    factory { UpdateGameTableUseCase(get()) }

    // 只读表查询门面：
    single { GameTableQuery(get()) }
}
