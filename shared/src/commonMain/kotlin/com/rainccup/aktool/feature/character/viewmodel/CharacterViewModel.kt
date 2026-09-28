package com.rainccup.aktool.feature.character.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.common.replace
import com.rainccup.aktool.core.domain.model.CharacterFilter
import com.rainccup.aktool.core.domain.usecase.character.CharacterUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.char_id
import com.rainccup.aktool.resources.copy_success
import com.rainccup.aktool.resources.game_table_init_fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val useCase: CharacterUseCase,
    private val messenger: Messenger,
    private val clipboard: ClipboardPort,
) : ViewModel() {
    val characterData = mutableMapOf<String, Character>()
    private val charNameMap = mutableMapOf<String, String>()

    /**
     * 已排序的干员列表，直接交给 `LazyVerticalGrid`。
     *
     * 每次变更都经 [CharacterFilter] 排序（`byProfession` 内部 `sorted()`），所以这里无需再排。
     * 原先页面调用的是 `charList()`，那个方法会在**组合期**对正在被网格遍历的列表排序——
     * 既浪费又破坏"组合不产生副作用"的约定。
     */
    val characters: List<Character>
        field = mutableStateListOf<Character>()

    val splash: StateFlow<Boolean>
        field = MutableStateFlow(true)

    val isSelect: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val profession: StateFlow<String>
        field = MutableStateFlow("ALL")

    val loadAnimate: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val gainChar: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isSearch: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun initCharData() = viewModelScope.launch {
        if (!loadAnimate.value) {
            if (!useCase.init()) {
                messenger.show(getString(Res.string.game_table_init_fail))
                return@launch
            }
            loadAnimate.emit(true)
            characterData.clear()
            runCatching {
                val loaded = useCase.loadCharacters()
                characterData.putAll(loaded.characters)
                charNameMap.clear()
                charNameMap.putAll(loaded.nameToId)
                selectProfession(profession.value)
                delay(500.milliseconds)
            }.onFailure {
                messenger.show(it.message ?: "error")
            }
            closeAnimate()
        }
    }

    fun existChar(charId: String): Boolean = useCase.existChar(charId)

    fun selectProfession(profession: String) = viewModelScope.launch {
        this@CharacterViewModel.profession.emit(profession)
        characters.clear()
        characters.addAll(CharacterFilter.byProfession(characterData.values, profession))
    }

    fun changeSelectState(state: Boolean) = viewModelScope.launch {
        isSelect.emit(state)
    }

    fun updateCharData(char: Character) = viewModelScope.launch {
        characterData[char.instId.toString()] = char
        selectProfession(profession.value)
    }

    fun closeAnimate() = viewModelScope.launch {
        splash.emit(false)
        loadAnimate.emit(false)
    }

    /**
     * 补发干员。异常必须在这里报告：gainCharacter 由本协程启动，
     * 调用方无法用 runCatching 捕获（页面原先就是这么写的，那个 catch 永远是空的）。
     * 成功后刷新列表。
     */
    fun gainChar(charId: String) = viewModelScope.launch {
        runCatching { useCase.gainCharacter(charId) }
            .onSuccess { initCharData() }
            .onFailure { messenger.show(it.message ?: "error") }
    }

    fun changeGainCharState() = viewModelScope.launch {
        gainChar.emit(gainChar.value.not())
    }

    fun getSearchedCharList(charName: String): List<String> =
        CharacterFilter.matches(charNameMap, charName)

    fun copyCharId(charName: String) = viewModelScope.launch {
        val charId = CharacterFilter.idOf(charNameMap, charName)
        if (charId == CharacterFilter.NOT_FOUND) {
            messenger.show(getString(Res.string.char_id))
            return@launch
        }
        clipboard.setText(charId)
        messenger.show(getString(Res.string.copy_success, charId))
    }

    fun changeSearchState() = viewModelScope.launch {
        isSearch.emit(isSearch.value.not())
    }

    fun getCharIdByCharName(charName: String): String =
        CharacterFilter.idOf(charNameMap, charName)

    fun searchChar(charName: String) {
        characterData.values.find { it.name == charName }?.let {
            characters.replace(it)
        }
    }
}
