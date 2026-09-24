package com.rainccup.aktool.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Home : AppRoute
    @Serializable data object Character : AppRoute
    @Serializable data object Extra : AppRoute
    @Serializable data object Setting : AppRoute
    @Serializable data class CharacterDetail(val charInstId: String) : AppRoute
}
