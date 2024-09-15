package com.yaroslavzghoba.model

import com.yaroslavzghoba.security.hashing.HashingService

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
    class Builder(
        private val credentials: Credentials,
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
            username = credentials.username,
            hashedPassword = hashingService
                .hash(password = credentials.password, salt = _salt),
            salt = _salt,
        )
    }
}