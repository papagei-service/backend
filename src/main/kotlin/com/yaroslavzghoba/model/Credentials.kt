package com.yaroslavzghoba.model

import kotlinx.serialization.Serializable

/**
 * Represents credentials received from the client.
 *
 * @param username A unique identifier of the user's account.
 * @param password A special code word that used to log in the system.
 */
@Serializable
data class Credentials(
    val username: String,
    val password: String,
) {
    /**
     * Convert the instance of the [Credentials] class.
     * to an instance of the [User] class with default properties.
     *
     * @return An instance of the [User] class.
     */
    fun toNewUser(): User {
        return User(
            username = username,
            password = password,
        )
    }
}
