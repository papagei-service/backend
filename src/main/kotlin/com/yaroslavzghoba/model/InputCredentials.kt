package com.yaroslavzghoba.model

import kotlinx.serialization.Serializable

/**
 * Represents the credentials sent by the client.
 *
 * @param username A unique identifier of the user's account.
 * @param password A special code word that used to log in the system.
 */
@Serializable
data class InputCredentials(
    val username: String,
    val password: String,
)