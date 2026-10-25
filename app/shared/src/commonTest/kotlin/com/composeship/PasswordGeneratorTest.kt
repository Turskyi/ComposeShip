package com.composeship

import com.composeship.password.PasswordOptions
import com.composeship.password.canGeneratePasswords
import com.composeship.password.generatePasswords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordGeneratorTest {
    @Test
    fun defaultsGenerateFourUniqueSixteenCharacterPasswords() {
        val passwords = generatePasswords()

        assertEquals(4, passwords.size)
        passwords.forEach { password ->
            assertEquals(16, password.length)
            assertTrue(password.first().isLetter())
            assertEquals(password.length, password.toSet().size)
            assertTrue(password.none { it in "il1ILO0o" })
            assertTrue(password.zipWithNext().none { (first, second) ->
                kotlin.math.abs(first.code - second.code) == 1
            })
        }
    }

    @Test
    fun invalidSettingsAreRejectedBeforeGeneration() {
        assertFalse(canGeneratePasswords(3, 4, PasswordOptions()))
        assertFalse(canGeneratePasswords(16, 0, PasswordOptions()))
        assertFalse(
            canGeneratePasswords(
                16,
                4,
                PasswordOptions(includeNumbers = false, includeLowercase = false, includeUppercase = false)
            )
        )
        assertFalse(
            canGeneratePasswords(
                16,
                4,
                PasswordOptions(
                    includeNumbers = true,
                    includeLowercase = false,
                    includeUppercase = false,
                    excludeSimilarCharacters = false
                )
            )
        )
        assertFalse(canGeneratePasswords(55, 4, PasswordOptions()))
    }
}
