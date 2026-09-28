package com.rainccup.aktool.feature.characterdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.character.CharacterDetailUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.Messenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val useCase: CharacterDetailUseCase,
    private val messenger: Messenger,
    char: Character
) : ViewModel() {
    val character: StateFlow<Character>
        field = MutableStateFlow(
            char.copy(favorPoint = useCase.favPointPercent(char.favorPoint))
        )

    val maxEvoPhase: StateFlow<Int>
        field = MutableStateFlow(2)

    val maxLevel: StateFlow<Int>
        field = MutableStateFlow(90)
    val maxSkillLevel: StateFlow<Int>
        field = MutableStateFlow(7)

    init {
        viewModelScope.launch {
            val limits = useCase.limits(char.charId, char.evolvePhase)
            maxEvoPhase.emit(limits.maxEvoPhase)
            maxLevel.emit(limits.maxLevel)
            maxSkillLevel.emit(limits.maxSkillLevel)
        }
    }

    fun accept(char: Character) = viewModelScope.launch {
        character.emit(char)
    }

    /** 保存前的规则补全（模组解锁/默认选中/皮肤切换/等级满时清经验），返回补全后的角色。 */
    fun applySaveRules(char: Character): Character = useCase.applySaveRules(char)

    fun saveCharData(char: Character) = viewModelScope.launch {
        runCatching {
            val result = useCase.saveCharacter(char)
            if (result.status != 0) {
                throw RuntimeException(result.msg)
            }
        }.onSuccess { messenger.showSuccess() }
            .onFailure { messenger.show(it.message ?: "error") }
    }

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        val change = useCase.changeEvolvePhase(character.value, phase)
        maxLevel.emit(change.maxLevel)
        maxSkillLevel.emit(change.maxSkillLevel)
        character.emit(change.character)
    }

}
