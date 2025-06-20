package com.yaroslavzghoba.model

import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent a user of the service.
 *
 * @param id A unique identifier of the user's account that cannot be changed.
 * @param username A unique identifier of the user's account that can be changed.
 * @param displayName A username, but not unique. Intended for display purposes.
 * @param hashedPassword The value of the hash of the user's password structure, its [salt] and the paper.
 * @param salt A special code that is added to a user's password when it is hashed.
 */
@Serializable
data class User(
    @SerialName("id") val id: Long?,
    @SerialName("username") val username: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("hashed_password") val hashedPassword: String,
    @SerialName("salt") val salt: String,
) {
    /**
     * Instance builder of the [User] class. Used when registering new users in the system.
     *
     * @param registrationCredentials Registration credentials provided by the user for registration.
     * @param hashingService Service for password hashing.
     *
     * @sample userBuildingSample
     */
    class Builder(
        private val registrationCredentials: RegistrationCredentials,
        private val hashingService: HashingService,
    ) {
        private var _salt: String = ""

        /**
         * Set the salt to be added to the user password when hashing.
         *
         * @param salt The salt to be added to the user's password.
         */
        fun withSalt(salt: String): Builder {
            _salt = salt
            return this
        }

        /**
         * Create and return a user with specified properties.
         */
        fun build() = User(
            id = null,
            username = registrationCredentials.username,
            displayName = registrationCredentials.displayName,
            hashedPassword = hashingService
                .hash(password = registrationCredentials.password, salt = _salt),
            salt = _salt,
        )
    }
}

/**
 * An example of using the [User.Builder] of instances of the [User] class.
 */
@Suppress("unused")
private fun userBuildingSample() {
    val registrationCredentials =
        RegistrationCredentials(username = "admin", displayName = "Admin", password = "qwerty")
    val hashingService = HashingServiceImpl(pepper = "pepper", algorithm = "SHA-512")
    val user = User.Builder(registrationCredentials = registrationCredentials, hashingService = hashingService)
        .withSalt(salt = "salt")
        .build()
    // Do something really useful with it
}