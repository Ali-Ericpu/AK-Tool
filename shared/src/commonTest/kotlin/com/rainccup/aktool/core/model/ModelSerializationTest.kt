package com.rainccup.aktool.core.model

import com.rainccup.aktool.core.common.JsonUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelSerializationTest {

    @Test
    fun resultEnvelope_roundTrip() {
        val raw =
            """{"msg":"ok","status":0,"type":"OK","data":{"id":"x","type":"CHAR","count":1}}"""
        val decoded = JsonUtil.decode<ApiResult<Item>>(raw)
        assertEquals(0, decoded.status)
        assertEquals("x", decoded.data?.id)
        val re = JsonUtil.decode<ApiResult<Item>>(JsonUtil.encode(decoded))
        assertEquals(decoded, re)
    }

    @Test
    fun saveStatusRequest_omitsNulls() {
        val req = SaveStatusRequest(level = 10)
        val text = JsonUtil.encode(req)
        assertTrue("\"level\":10" in text)
        assertFalse("nickName" in text)
    }

    @Test
    fun appConfig_defaults() {
        val cfg = JsonUtil.decode<AppConfig>("""{"serverUri":"http://127.0.0.1"}""")
        assertEquals("http://127.0.0.1", cfg.serverUri)
        assertEquals(false, cfg.darkMode)
        assertEquals("", cfg.bgPath)
    }

    @Test
    fun character_minimumFields() {
        val raw = """
            {"instId":1,"charId":"char_002_amiya","favorPoint":0,"potentialRank":0,
             "mainSkillLvl":1,"skin":"char_002_amiya#1","level":1,"exp":0,"evolvePhase":0,
             "defaultSkillIndex":0,"gainTime":0,"skills":[],"voiceLan":"CN",
             "equip":{},"starMark":0}
        """.trimIndent()
        val char = JsonUtil.decode<Character>(raw)
        assertEquals("char_002_amiya", char.charId)
        assertEquals(null, char.name)
    }
}
