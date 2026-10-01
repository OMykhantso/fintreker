package com.example.fintreker.data

import com.example.fintreker.data.remote.NbuRateDto
import com.example.fintreker.data.remote.toEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NbuMapperTest {

    private fun dto(code: String? = "USD", rate: Double? = 41.5, date: String? = "01.10.2026") =
        NbuRateDto(numericCode = 840, name = "Долар США", rate = rate, code = code, exchangeDate = date)

    @Test
    fun `maps a valid record`() {
        val entity = dto().toEntity(updatedAt = 99L)!!

        assertEquals("USD", entity.code)
        assertEquals(41.5, entity.rate, 0.0)
        assertEquals("01.10.2026", entity.exchangeDate)
        assertEquals(99L, entity.updatedAt)
    }

    @Test
    fun `normalizes currency code`() {
        assertEquals("EUR", dto(code = " eur ").toEntity(0L)!!.code)
    }

    @Test
    fun `rejects records without code`() {
        assertNull(dto(code = null).toEntity(0L))
        assertNull(dto(code = "   ").toEntity(0L))
    }

    @Test
    fun `rejects zero negative and non finite rates`() {
        listOf(null, 0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { rate ->
            assertNull("rate=$rate", dto(rate = rate).toEntity(0L))
        }
    }

    @Test
    fun `missing date becomes empty string`() {
        assertEquals("", dto(date = null).toEntity(0L)!!.exchangeDate)
    }

    @Test
    fun `parses the real NBU json shape`() {
        val json = """
            [
              {"r030":840,"txt":"Долар США","rate":41.2345,"cc":"USD","exchangedate":"01.10.2026"},
              {"r030":978,"txt":"Євро","rate":48.9,"cc":"EUR","exchangedate":"01.10.2026"},
              {"r030":999,"txt":"Зламаний запис","cc":"XXX","exchangedate":"01.10.2026"}
            ]
        """.trimIndent()

        val dtos: List<NbuRateDto> = Gson().fromJson(json, object : TypeToken<List<NbuRateDto>>() {}.type)
        val entities = dtos.mapNotNull { it.toEntity(1L) }

        assertEquals(3, dtos.size)
        assertEquals(listOf("USD", "EUR"), entities.map { it.code })
        assertEquals(41.2345, entities.first().rate, 0.0)
        assertNotNull(dtos[2])
        assertNull(dtos[2].rate)
    }
}
