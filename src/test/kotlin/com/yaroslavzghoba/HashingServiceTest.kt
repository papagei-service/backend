package com.yaroslavzghoba

import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HashingServiceTest {

    @Test
    fun `A hash must be of the same length`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val hash0 = hashingService.hash(password = "qwerty", salt = "V!g4JE0^GpD")
            val hash1 = hashingService.hash(password = "password", salt = "z?Mc0#HXW2u63eELewcWK")
            assertEquals(hash0.length, hash1.length)
        }
    }

    @Test
    fun `Returns the same hash for the same input`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val (password, salt) = listOf("qwerty", "V!g4JE0^GpD")
            val hash0 = hashingService.hash(password = password, salt = salt)
            val hash1 = hashingService.hash(password = password, salt = salt)
            assertEquals(hash0, hash1)
        }
    }

    @Test
    fun `Returns a different hash for different peppers`() = testConfiguredApplication {
        serverConfig {
            val (pepper0, pepper1) = listOf("J?UIR4Q*mYn", "ZP4gYd^sAt2")
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService0 = HashingServiceImpl(pepper = pepper0, algorithm = algorithm)
            val hashingService1 = HashingServiceImpl(pepper = pepper1, algorithm = algorithm)

            val (password, salt) = listOf("qwerty", "V!g4JE0^GpD")
            val hash0 = hashingService0.hash(password = password, salt = salt)
            val hash1 = hashingService1.hash(password = password, salt = salt)
            assertNotEquals(hash0, hash1)
        }
    }

    @Test
    fun `Returns a different hash for different salts`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val password = "qwerty"
            val (salt0, salt1) = listOf("J?UIR4Q*mYn", "ZP4gYd^sAt2")
            val hash0 = hashingService.hash(password = password, salt = salt0)
            val hash1 = hashingService.hash(password = password, salt = salt1)
            assertNotEquals(hash0, hash1)
        }
    }

    @Test
    fun `Returns a different hash for different passwords`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val (password0, password1) = listOf("qwerty", "password")
            val salt = "J?UIR4Q*mYn"
            val hash0 = hashingService.hash(password = password0, salt = salt)
            val hash1 = hashingService.hash(password = password1, salt = salt)
            assertNotEquals(hash0, hash1)
        }
    }
}