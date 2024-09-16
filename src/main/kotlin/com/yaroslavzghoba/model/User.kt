package com.yaroslavzghoba.model

import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.HashingServiceImpl

/**
 * Represent a user of the service.
 *
 * @param username A unique identifier of the user's account.
 * @param hashedPassword The value of the hash of the user's password structure, its [salt] and the papper.
 * @param salt A special code that is added to a user's password when it is hashed.
 */
data class User(
    val username: String,
    val hashedPassword: String,
    val salt: String,
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