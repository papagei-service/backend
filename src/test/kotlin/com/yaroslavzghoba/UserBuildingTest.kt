package com.yaroslavzghoba

import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.utils.MockData
import com.yaroslavzghoba.utils.testConfiguredApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBuildingTest {

    @Test
    fun `001= The builder does not distort the data`() = testConfiguredApplication { _, applicationConfig ->
        val pepper = applicationConfig.property("security.hashing.pepper").getString()
        val algorithm = applicationConfig.property("security.hashing.algorithm").getString()
        val hashingService = HashingServiceImpl(pepper = pepper, algorithm = algorithm)

        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val salt = MockData.FIRST_SALT
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