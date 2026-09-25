package com.rainccup.aktool.ui.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class CharacterDetailViewModel(
    private val gameTable: GameTableRepository,
    charInstId: String,
) : ViewModel() {
    val charInstId: StateFlow<String>
        field = MutableStateFlow(charInstId)

    val character: StateFlow<Character>
        field = MutableStateFlow(Character.placeholder())

    val maxEvoPhase: StateFlow<Int>
        field = MutableStateFlow(2)
    val maxLevel: StateFlow<Int>
        field = MutableStateFlow(90)

    val maxSkillLevel: StateFlow<Int>
        field = MutableStateFlow(7)

    fun accept(char: Character) = viewModelScope.launch {
        character.emit(char)
    }

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        var skillIndex = -1
        val char = character.value
        val skills = char.skills.mapIndexed { index, skill ->
            val unlock = if (phase >= index) {
                skillIndex = index
                1
            } else {
                0
            }
            skill.copy(unlock = unlock)
        }.toList()
        var currentEquip = char.currentEquip
        val equip = char.equip.mapValues { (equipId, data) ->
            if (phase >= 2) {
                currentEquip = equipId
                data.unlock()
            } else {
                currentEquip = null
                data.lock()
            }
        }
        val level = gameTable.getMaxCharLevel(char.charId, phase)
        val skillLevel = if (phase < 1) 4 else 7


        maxLevel.emit(level)
        maxSkillLevel.emit(skillLevel)
        character.emit(
            char.copy(
                evolvePhase = phase,
                mainSkillLvl = min(maxSkillLevel.value, char.mainSkillLvl),
                skills = skills,
                defaultSkillIndex = skillIndex,
                currentEquip = currentEquip,
                equip = equip,
                level = min(maxLevel.value, char.level),
            )
        )
    }

    fun updateMaxEvoPhase(charId: String) = viewModelScope.launch {
        maxEvoPhase.emit(gameTable.getMaxCharEvoLevel(charId))
    }

    fun updateMaxLevel(charId: String, phase: Int) = viewModelScope.launch {
        maxLevel.emit(gameTable.getMaxCharLevel(charId, phase))
    }

    fun updateMaxSkillLevel(phase: Int) = viewModelScope.launch {
        maxSkillLevel.emit(if (phase < 1) 4 else 7)
    }
}
