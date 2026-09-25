package com.rainccup.aktool.core.datastore

import com.rainccup.aktool.core.model.AppConfig
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.network.NetworkConfig
import com.rainccup.aktool.core.network.createHttpClient
import com.rainccup.aktool.core.platform.AppPaths
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameTableRepositoryTest {

    private val charJson = """
        {"char_002_amiya":{"name":"阿米娅","displayNumber":"R001","profession":"CASTER",
         "rarity":"5","phases":[{"maxLevel":40},{"maxLevel":55},{"maxLevel":80}]}}
    """.trimIndent()

    @Test
    fun seed_accessors() {
        val repo = GameTableRepository(FakePaths, api())
        repo.seedForTest(
            character = charJson,
            favor = """{"favorFrames":[{"level":0,"data":{"percent":0}}]}""",
            uniequip = """{"equipDict":{"equip_x":{"typeIcon":"original"}}}""",
        )
        assertEquals("阿米娅", repo.getCharacterData("char_002_amiya")["name"])
        assertEquals(2, repo.getMaxCharEvoLevel("char_002_amiya"))
        assertEquals(80, repo.getMaxCharLevel("char_002_amiya", 2))
        assertEquals(0, repo.getRealFavPoint(0))
        assertEquals("original", repo.getEquipType("equip_x"))
    }

    @Test
    fun init_returnsFalseWhenMissing() {
        val repo = GameTableRepository(FakePaths, api())
        assertFalse(repo.init())
    }

    private fun api(): ApiClient {
        val config = NetworkConfig("http://t")
        val engine = MockEngine { respond("{}", HttpStatusCode.OK) }
        return ApiClient(config) { createHttpClient(config, engine) }
    }

    private object FakePaths : AppPaths {
        override val configDir: String = "/cfg"
        override val dataDir: String = "/data"
        override fun resolve(relativePath: String): String = "/$relativePath"
    }
}

class ConfigRepositoryTest {
    @Test
    fun writeThenRead_roundTrip() {
        val base = "${System.getProperty("java.io.tmpdir")}/aktool-test-${System.nanoTime()}"
        val paths = object : AppPaths {
            override val configDir: String = "$base/config"
            override val dataDir: String = "$base/data"
            override fun resolve(relativePath: String): String = "$base/$relativePath"
        }
        val repo = ConfigRepository(paths)
        val cfg = AppConfig(serverUri = "http://127.0.0.1:8080", uid = "9", adminKey = "k")
        repo.write(cfg)
        assertEquals(cfg, repo.read())
    }
}
