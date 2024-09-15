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
)