package com.rainccup.aktool.feature.characterdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.domain.usecase.character.CharacterDetailUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.AppScope
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.save_success
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class CharacterDetailViewModel(
    private val useCase: CharacterDetailUseCase,
    private val messenger: Messenger,
    private val appScope: AppScope,
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

    val skinList: StateFlow<List<Pair<String, String>>>
        field = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            val limits = useCase.limits(char.charId, char.evolvePhase)
            maxEvoPhase.emit(limits.maxEvoPhase)
            maxLevel.emit(limits.maxLevel)
            maxSkillLevel.emit(limits.maxSkillLevel)
            skinList.emit(useCase.getSkinList(char.charId))
        }
    }

    fun accept(char: Character) = viewModelScope.launch {
        character.emit(char)
    }

    /** 保存前的规则补全（模组解锁/默认选中/皮肤切换/等级满时清经验），返回补全后的角色。 */
    fun applySaveRules(char: Character): Character = useCase.applySaveRules(char)

    fun saveCharData(char: Character) {
        // 必须在应用级作用域里保存：调用方启动保存后通常会立刻返回上一页，
        // viewModelScope 会随页面销毁被取消 —— 请求半路夭折，并抛出 CancellationException
        appScope.launch(Dispatchers.IO) {
            Logger.i(tag = "CHAR") { char.toString() }
            try {
                val result = useCase.saveCharacter(char)
                if (result.status != 0) {
                    throw RuntimeException(result.msg)
                }
                messenger.show(getString(Res.string.save_success))
            } catch (e: CancellationException) {
                // 取消不是业务错误：原样抛出让协程按取消语义结束，不要弹给用户。
                throw e
            } catch (e: Throwable) {
                messenger.show(e.message ?: "error")
            }
        }
    }

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        val change = useCase.changeEvolvePhase(character.value, phase)
        maxLevel.emit(change.maxLevel)
        maxSkillLevel.emit(change.maxSkillLevel)
        character.emit(change.character)
    }

}
