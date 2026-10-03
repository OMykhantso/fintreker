package com.example.fintreker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinTest {

    @Test
    fun `valid pin is exactly four ascii digits`() {
        listOf("0000", "1234", "9999").forEach { assertTrue("pin=$it", isValidPin(it)) }
    }

    @Test
    fun `invalid pins are rejected`() {
        listOf("", "123", "12345", "12a4", " 123", "12 4", "١٢٣٤", "-123", "1.23").forEach {
            assertFalse("pin=$it", isValidPin(it))
        }
    }

    @Test
    fun `hash is salted sha256 of salt plus pin`() {
        // SHA-256("abc") — відомий тестовий вектор
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hashPin(pin = "c", salt = "ab")
        )
    }

    @Test
    fun `hash is deterministic and never contains the pin`() {
        val first = hashPin("1234", "salt")
        assertEquals(first, hashPin("1234", "salt"))
        assertEquals(64, first.length)
        assertFalse(first.contains("1234"))
    }

    @Test
    fun `different salt or pin gives different hash`() {
        assertNotEquals(hashPin("1234", "salt-a"), hashPin("1234", "salt-b"))
        assertNotEquals(hashPin("1234", "salt"), hashPin("1235", "salt"))
    }

    @Test
    fun `pinMatches accepts only the original pin`() {
        val salt = generateSalt()
        val hash = hashPin("4321", salt)

        assertTrue(pinMatches("4321", salt, hash))
        assertFalse(pinMatches("1234", salt, hash))
        assertFalse(pinMatches("", salt, hash))
        assertFalse(pinMatches("4321", generateSalt(), hash))
    }

    @Test
    fun `salt is 128 bit hex and random`() {
        val a = generateSalt()
        val b = generateSalt()

        assertEquals(32, a.length)
        assertTrue(a.all { it in '0'..'9' || it in 'a'..'f' })
        assertNotEquals(a, b)
    }
}
