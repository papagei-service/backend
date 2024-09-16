package com.yaroslavzghoba

import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBuildingTest {

    private val inputCredentials =
        InputCredentials(username = "yaroslav", password = "qwerty")
    private val salt = "89#vaQ1!PG*LqI!8z"

    @Test
    fun `The builder does not distort the data`() = testConfiguredApplication {
        serverConfig {
            val pepper = environment.config.property("security.hashing.pepper").getString()
            val algorithm = environment.config.property("security.hashing.algorithm").getString()
            val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

            val buildedUser = User.Builder(inputCredentials = inputCredentials, hashingService = hashingService)
                .withSalt(salt = salt)
                .build()
            val hashedPassword = hashingService
                .hash(password = inputCredentials.password, salt = salt)
            val user = User(username = inputCredentials.username, hashedPassword = hashedPassword, salt = salt)

            assertEquals(user.toString(), buildedUser.toString())
        }
    }
}