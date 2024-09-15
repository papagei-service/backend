package com.yaroslavzghoba

import com.yaroslavzghoba.model.Credentials
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBuildingTest {

    private val credentials =
        Credentials(username = "yaroslav", password = "qwerty")
    private val salt = "89#vaQ1!PG*LqI!8z"

    @Test
    fun `The builder does not distort the data`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val buildedUser = User.Builder(credentials = credentials, hashingService = hashingService)
                .withSalt(salt = salt)
                .build()
            val hashedPassword = hashingService
                .hash(password = credentials.password, salt = salt)
            val user = User(username = credentials.username, hashedPassword = hashedPassword, salt = salt)

            assertEquals(user.toString(), buildedUser.toString())
        }
    }
}