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
 * @param hashedPassword The value of the hash of the user's password structure, its [salt] and the paper.
 * @param salt A special code that is added to a user's password when it is hashed.
 */
@Serializable
data class User(
    @SerialName("id") val id: Long?,
    @SerialName("username") val username: String,
    @SerialName("hashed_password") val hashedPassword: String,
    @SerialName("salt") val salt: String,
) {
    /**
     * Instance builder of the [User] class. Used when signing up new users in the system
     *
     * @param inputCredentials Credentials for further sign in.
     * @param hashingService Service for password hashing.
     *
     * @sample userBuildingSample
     */
    class Builder(
        private val inputCredentials: InputCredentials,
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
            username = inputCredentials.username,
            hashedPassword = hashingService
                .hash(password = inputCredentials.password, salt = _salt),
            salt = _salt,
        )
    }
}

/**
 * An example of using the [User.Builder] of instances of the [User] class.
 */
private fun userBuildingSample() {
    val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
    val hashingService = HashingServiceImpl(pepper = "pepper", algorithm = "SHA-512")
    val user = User.Builder(inputCredentials = inputCredentials, hashingService = hashingService)
        .withSalt(salt = "salt")
        .build()
    // Do something really useful with it
}