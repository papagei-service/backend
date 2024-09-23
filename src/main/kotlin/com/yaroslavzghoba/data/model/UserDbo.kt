package com.yaroslavzghoba.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent an object of the service user that can be stored in the database.
 *
 * @param username A unique identifier of the user's account.
 * @param hashedPassword The value of the hash of the user's password structure, its [salt] and the papper.
 * @param salt A special code that is added to a user's password when it is hashed.
 */
@Serializable
data class UserDbo(
    @SerialName("username") val username: String,
    @SerialName("hashedPassword") val hashedPassword: String,
    @SerialName("salt") val salt: String,
)
