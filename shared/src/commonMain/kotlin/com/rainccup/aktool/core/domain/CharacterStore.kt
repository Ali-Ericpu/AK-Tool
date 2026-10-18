package com.rainccup.aktool.core.domain

import com.rainccup.aktool.core.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 干员数据的唯一来源（single source of truth）。
 *
 * 列表页与详情页都直接读写这里：详情页保存后调用 [upsert]，列表页只观察派生结果。
 * 不再经由导航参数或路由对象回传结果——那条老路是把可变字段塞进 `@Serializable` 的路由里，
 * 既不是快照状态（写入不触发重组，只靠别的重组"顺带"生效），也无法随状态保存恢复。
 */
class CharacterStore {

    /** 以 `instId` 为键的全部干员。 */
    val characters: StateFlow<Map<String, Character>>
        field = MutableStateFlow<Map<String, Character>>(emptyMap())

    /** 名称 -> charId，搜索与补发时用。 */
    val names: StateFlow<Map<String, String>>
        field = MutableStateFlow<Map<String, String>>(emptyMap())

    fun replaceAll(chars: Map<String, Character>, name: Map<String, String>) {
        characters.value = chars
        names.value = name
    }

    /**
     * 只替换一个干员。
     *
     * `Map + (key to value)` 只改这一个条目，其余条目对象身份不变；列表页据此重算可见列表时，
     * 未变化的项不会被重新创建，滚动位置与项内状态都得以保留。
     */
    fun upsert(char: Character) {
        characters.update { it + (char.instId.toString() to char) }
    }
}
