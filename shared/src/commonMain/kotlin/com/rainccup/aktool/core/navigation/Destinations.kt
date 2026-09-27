package com.rainccup.aktool.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Home : AppRoute
    @Serializable data object Character : AppRoute {
        @Volatile
        var char : com.rainccup.aktool.core.model.Character? = null
    }
    @Serializable data object Extra : AppRoute
    @Serializable data object Setting : AppRoute
    @Serializable data class CharacterDetail(val char: com.rainccup.aktool.core.model.Character) : AppRoute
}
