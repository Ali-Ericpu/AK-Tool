package com.rainccup.aktool.feature.character.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.CharacterStore
import com.rainccup.aktool.core.domain.model.CharacterFilter
import com.rainccup.aktool.core.domain.usecase.character.CharacterUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.char_id
import com.rainccup.aktool.resources.character_not_exist
import com.rainccup.aktool.resources.copy_success
import com.rainccup.aktool.resources.game_table_init_fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val useCase: CharacterUseCase,
    private val store: CharacterStore,
    private val messenger: Messenger,
    private val clipboard: ClipboardPort,
) : ViewModel() {

    val splash: StateFlow<Boolean>
        field = MutableStateFlow(true)

    val isSelect: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val loadAnimate: StateFlow<Boolean>
        field = MutableStateFlow(false)

    /** 当前职业筛选。 */
    val profession: StateFlow<String>
        field = MutableStateFlow("ALL")

    /** 搜索关键字；null 表示没在搜索。 */
    private val queryFlow = MutableStateFlow<String?>(null)

    /** 从搜索结果里"选中某个名字"后，只显示那一个干员。 */
    private val pickedFlow = MutableStateFlow<Int?>(null)

    /**
     * 可见列表 = 唯一数据源 + 职业筛选 + 搜索，**派生**而来。
     *
     * 详情页保存时只往 [CharacterStore] 里写一个干员，这里就会自动重算可见列表：
     * 不需要让列表页整表重建（旧实现每次 `clear()+addAll()`，会丢掉所有项内状态），
     * 也不需要经由导航参数/路由字段回传结果。
     */
    val characters: StateFlow<List<Character>> =
        combine(
            store.characters,
            profession,
            queryFlow,
            pickedFlow,
        ) { all, profession, query, picked ->
            all.values
                .filter { profession == "ALL" || it.profession == profession }
                .filter { picked == null || it.instId == picked }
                .filter { query.isNullOrEmpty() || it.name?.contains(query) == true }
                .sorted()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun initCharData() = viewModelScope.launch {
        if (!loadAnimate.value) {
            if (!useCase.init()) {
                messenger.show(getString(Res.string.game_table_init_fail))
                return@launch
            }
            loadAnimate.emit(true)
            runCatching {
                val loaded = useCase.loadCharacters()
                store.replaceAll(loaded.characters, loaded.nameToId)
                selectProfession("ALL")
                delay(500.milliseconds)
            }.onFailure {
                messenger.show(it.message ?: "error")
            }
            closeAnimate()
        }
    }

    fun existChar(charId: String): Boolean =
        useCase.existChar(charId) || store.names.value[charId] != null

    /** 切换职业筛选：顺带清掉搜索/选中状态，回到"该职业的全部干员"。 */
    fun selectProfession(select: String) {
        queryFlow.value = null
        pickedFlow.value = null
        profession.value = select
    }

    fun changeSelectState(state: Boolean) = viewModelScope.launch {
        isSelect.emit(state)
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
        val realCharId = if (useCase.existChar(charId)) charId else getCharIdByCharName(charId)
        runCatching { useCase.gainCharacter(realCharId) }
            .onSuccess { initCharData() }
            .onFailure { messenger.show(it.message ?: "error") }
    }

    fun getSearchedCharList(charName: String): List<String> =
        CharacterFilter.matches(store.names.value, charName)

    fun copyCharId(charName: String) = viewModelScope.launch {
        val charId = getCharIdByCharName(charName)
        if (charId == CharacterFilter.NOT_FOUND) {
            messenger.show(getString(Res.string.char_id))
            return@launch
        }
        clipboard.setText(charId)
        messenger.show(getString(Res.string.copy_success, charId))
    }

    fun getCharIdByCharName(charName: String): String =
        CharacterFilter.idOf(store.names.value, charName)

    fun pickName(charName: String) {
        val found = store.characters.value.values.find { it.name == charName }
        if (found == null) {
            viewModelScope.launch { messenger.show(getString(Res.string.character_not_exist)) }
        } else {
            queryFlow.value = null
            pickedFlow.value = found.instId
            profession.value = found.profession!!
        }
    }

    fun searchChar(charName: String?) {
        pickedFlow.value = null
        queryFlow.value = charName
    }
}
