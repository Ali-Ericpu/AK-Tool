package com.rainccup.aktool.app

import com.rainccup.aktool.core.common.JsonUtil
import com.rainccup.aktool.core.data.datasource.GameTableRepository
import com.rainccup.aktool.core.data.datasource.writeFileText
import com.rainccup.aktool.core.data.repository.AdminRepository
import com.rainccup.aktool.core.domain.usecase.character.CharacterUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.network.createHttpClient
import com.rainccup.aktool.core.platform.AppPaths
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `loadCharacters` 的解析规则回归测试。
 *
 * 放在 `app` 包：这个用例必须**同时**装配数据层（`ApiClient`/`AdminRepository`/
 * `GameTableRepository`）与域层（`CharacterUseCase`），而架构棘轮里只有 `app`
 * 被允许依赖全部层（`core.domain` 不许引 `core.network`/`core.platform`，
 * `core.data` 不许引 `core.domain`）。与 `AppModuleResolutionTest` 同层同由。
 *
 * 这里钉住三件事：
 *  1. name/profession/rarity 由数据表补全，rarity 的 `TIER_n` 要解析成数字；
 *  2. 名册里 charId 不在表中（或不是可展示条目）时**跳过**该角色，而不是让整次加载失败——
 *     这条正是把 `runCatching { getCharacterData(...) }` 换成判空后必须保持的行为；
 *  3. 未拥有的干员不进入结果，且 `nameToId` 只收录带 displayNumber 的 `char_` 条目。
 */
class CharacterUseCaseTest {

    private val charTable = """
        {
          "char_002_amiya":{"name":"阿米娅","displayNumber":"R001","profession":"CASTER","rarity":"TIER_5"},
          "char_010_chen":{"name":"陈","displayNumber":"R010","profession":"WARRIOR","rarity":"TIER_6"},
          "char_500_npc":{"name":"路人","profession":"MEDIC","rarity":"TIER_1"}
        }
    """.trimIndent()

    @Test
    fun fillsNameProfessionAndRarityFromTable() = runBlocking {
        val loaded = useCase(roster = listOf(inst(1, "char_002_amiya"), inst(2, "char_010_chen")))
            .loadCharacters()

        assertEquals(2, loaded.characters.size)
        val amiya = loaded.characters.getValue("1")
        assertEquals("阿米娅", amiya.name)
        assertEquals("CASTER", amiya.profession)
        assertEquals(5, amiya.rarity)
        assertEquals(6, loaded.characters.getValue("2").rarity)
    }

    @Test
    fun skipsCharactersMissingFromTheTable() = runBlocking {
        val loaded = useCase(
            roster = listOf(
                inst(1, "char_002_amiya"),
                inst(99, "char_999_unknown"),
            )
        ).loadCharacters()

        // 未知 charId 只跳过它自己，其余角色照常解析。
        assertEquals(1, loaded.characters.size)
        assertNull(loaded.characters["99"])
        assertEquals("阿米娅", loaded.characters.getValue("1").name)
    }

    @Test
    fun parsesAnyCharacterPresentInTheTableEvenWithoutDisplayNumber() = runBlocking {
        // `displayNumber` 只用于构建 nameToId（决定搜索候选名），**不**过滤名册：
        // 只要 charId 在表里就照常解析。实测运行中的应用该字段对全部 char_ 条目都非空，
        // 所以这条不是常见路径，但保持与原实现一致（原实现也只判"在不在表里"）。
        val loaded = useCase(roster = listOf(inst(500, "char_500_npc"))).loadCharacters()

        assertEquals(1, loaded.characters.size)
        val npc = loaded.characters.getValue("500")
        assertEquals("路人", npc.name)
        assertEquals("MEDIC", npc.profession)
        assertEquals(1, npc.rarity)
    }

    @Test
    fun nameToIdOnlyContainsDisplayableCharacterEntries() = runBlocking {
        val loaded = useCase(roster = listOf(inst(1, "char_002_amiya"))).loadCharacters()

        assertEquals(2, loaded.nameToId.size)
        assertEquals("char_002_amiya", loaded.nameToId["阿米娅"])
        assertEquals("char_010_chen", loaded.nameToId["陈"])
        // 没有 displayNumber 的条目不应出现。
        assertNull(loaded.nameToId["路人"])
    }

    @Test
    fun notOwnedCharactersAreAbsentFromResult() = runBlocking {
        // 只拥有阿米娅时，表里的"陈"不应出现在名册结果里。
        val loaded = useCase(roster = listOf(inst(1, "char_002_amiya"))).loadCharacters()

        assertEquals(setOf("1"), loaded.characters.keys)
    }

    // ---- helpers ----

    /** 服务端下发的干员（表字段全部留空，由 use case 从数据表补全）。 */
    private fun inst(instId: Int, charId: String) = Character(
        instId = instId,
        charId = charId,
        favorPoint = 0,
        potentialRank = 0,
        mainSkillLvl = 1,
        skin = "$charId#1",
        level = 1,
        exp = 0,
        evolvePhase = 0,
        defaultSkillIndex = 0,
        gainTime = 0L,
        voiceLan = "CN",
        starMark = 0,
    )

    private fun useCase(roster: List<Character>): CharacterUseCase {
        val paths = tempPaths()
        writeExcelTables(paths)

        val config = NetworkConfig("http://test")
        val engine = MockEngine { request ->
            val body = if (request.url.encodedPath.endsWith("character/sync")) {
                JsonUtil.encode(
                    com.rainccup.aktool.core.model.ApiResult(
                        msg = "ok",
                        status = 0,
                        type = "OK",
                        data = roster.associateBy { it.instId.toString() },
                    )
                )
            } else {
                """{"msg":"ok","status":0,"type":"OK"}"""
            }
            // ApiClient 用 Ktor 的 .body() 反序列化，依赖 ContentNegotiation，
            // 因此桩响应必须声明 Content-Type（真实服务端会带）。
            respond(
                body,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = ApiClient(config) { createHttpClient(config, engine) }
        val gameTable = GameTableRepository(paths, api)
        val admin = AdminRepository(
            api = api,
            configSource = object : com.rainccup.aktool.core.data.repository.ConfigSource {
                override fun current() = com.rainccup.aktool.core.model.AppConfig()
            },
            networkConfig = config,
        )
        val useCase = CharacterUseCase(admin, gameTable)
        // 与 CharacterViewModel.initCharData 的执行顺序一致：先读表，再解析名册。
        runBlocking { useCase.init() }
        return useCase
    }

    private fun tempPaths(): AppPaths {
        val base = "${System.getProperty("java.io.tmpdir")}/aktool-chartest-${System.nanoTime()}"
        return object : AppPaths {
            override val configDir: String = "$base/config"
            override val dataDir: String = "$base/data"
            override fun resolve(relativePath: String): String = "$base/$relativePath"
        }
    }

    /** 三张 excel 表就位，`GameTableRepository.init()` 才会返回 true。 */
    private fun writeExcelTables(paths: AppPaths) {
        val dir = "${paths.dataDir}/excel"
        writeFileText("$dir/character_table.json", charTable)
        writeFileText("$dir/favor_table.json", """{"favorFrames":[{"level":0,"data":{"percent":0}}]}""")
        writeFileText("$dir/uniequip_table.json", """{"equipDict":{}}""")
    }
}
