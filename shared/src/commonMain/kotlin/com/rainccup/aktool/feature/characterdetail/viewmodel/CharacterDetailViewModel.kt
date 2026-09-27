package com.rainccup.aktool.feature.characterdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.character.ChangeEvolvePhaseUseCase
import com.rainccup.aktool.core.domain.usecase.character.GetCharacterLimitsUseCase
import com.rainccup.aktool.core.domain.usecase.character.SaveCharacterUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val changeEvolvePhaseUseCase: ChangeEvolvePhaseUseCase,
    private val getCharacterLimits: GetCharacterLimitsUseCase,
    private val saveCharacter: SaveCharacterUseCase,
    private val messenger: Messenger,
    char: Character
) : ViewModel() {
    val character: StateFlow<Character>
        field = MutableStateFlow(
            char.copy(
                favorPoint = changeEvolvePhaseUseCase.getFavPointPercent(
                    char.favorPoint
                )
            )
        )

    val maxEvoPhase: StateFlow<Int>
        field = MutableStateFlow(2)

    val maxLevel: StateFlow<Int>
        field = MutableStateFlow(90)
    val maxSkillLevel: StateFlow<Int>
        field = MutableStateFlow(7)

    init {
        viewModelScope.launch {
            val characterLimits = getCharacterLimits(char.charId, char.evolvePhase)
            maxEvoPhase.emit(characterLimits.maxEvoPhase)
            maxLevel.emit(characterLimits.maxLevel)
            maxSkillLevel.emit(characterLimits.maxSkillLevel)
        }
    }

    fun accept(char: Character) = viewModelScope.launch {
        character.emit(char)
    }

    /** 保存前的规则补全（模组解锁/默认选中/皮肤切换/等级满时清经验），返回补全后的角色。 */
    fun applySaveRules(char: Character): Character = saveCharacter.applyRules(char)

    fun saveCharData(char: Character) = viewModelScope.launch {
        runCatching {
            val result = saveCharacter(char)
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
        }.onSuccess { messenger.showSuccess() }
            .onFailure { messenger.show(it.message ?: "error") }
    }

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        val change = changeEvolvePhaseUseCase(character.value, phase)
        maxLevel.emit(change.maxLevel)
        maxSkillLevel.emit(change.maxSkillLevel)
        character.emit(change.character)
    }

}
