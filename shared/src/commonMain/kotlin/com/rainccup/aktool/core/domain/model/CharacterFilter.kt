package com.rainccup.aktool.core.domain.model

import com.rainccup.aktool.core.model.Character

/**
 * 角色列表的排序 / 职业筛选 / 名称匹配规则。
 * 原先是 `CharacterViewModel` 里的派生读取，属于业务规则，因此下沉到 domain。
 */
object CharacterFilter {

    /** 未匹配到干员时的返回值（保留原字面量，调用方据此判断）。 */
    const val NOT_FOUND: String = "ERROR"

    fun byProfession(all: Collection<Character>, profession: String): List<Character> =
        if (profession == "ALL") all.sorted()
        else all.filter { it.profession == profession }.sorted()


    /** 名称子串匹配，最多返回 10 个（与原实现一致）。 */
    fun matches(nameToId: Map<String, String>, query: String): List<String> {
        if (query.isEmpty()) return emptyList()
        val found = mutableListOf<String>()
        for (name in nameToId.keys) {
            if (query in name) found.add(name)
            if (found.size == 10) break
        }
        return found
    }

    /** 干员名 → id；未找到返回 [NOT_FOUND]。 */
    fun idOf(nameToId: Map<String, String>, name: String): String = nameToId[name] ?: NOT_FOUND
}
