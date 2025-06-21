package com.yaroslavzghoba

import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.utils.TestData
import com.yaroslavzghoba.utils.testConfiguredApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBuildingTest {

    @Test
    fun `The builder does not distort the data`() = testConfiguredApplication { _, applicationConfig ->
        val pepper = applicationConfig.property("security.hashing.pepper").getString()
        val algorithm = applicationConfig.property("security.hashing.algorithm").getString()
        val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        val salt = TestData.FIRST_SALT
        // Building a user using `User` class
        val builtUser = User.Builder(registrationCredentials = registrationCredentials, hashingService = hashingService)
            .withSalt(salt = salt)
            .build()
        // Creating a user independently
        val hashedPassword = hashingService
            .hash(password = registrationCredentials.password, salt = salt)
        val user = User(
            id = null,
            username = registrationCredentials.username,
            displayName = registrationCredentials.displayName,
            hashedPassword = hashedPassword,
            salt = salt
        )

        assertEquals(user.toString(), builtUser.toString())
    }
}