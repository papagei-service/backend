package space.zghoba

import space.zghoba.model.User
import space.zghoba.security.hashing.HashingServiceImpl
import space.zghoba.utils.MockData
import space.zghoba.utils.testConfiguredApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBuildingTest {

    @Test
    fun `The builder does not distort the data`() = testConfiguredApplication { _, applicationConfig ->
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