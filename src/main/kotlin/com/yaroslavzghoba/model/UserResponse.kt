package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent an account of a user of the service.
 *
 * @param id A unique identifier of the user's account that cannot be changed.
 * @param username A unique identifier of the user's account that can be changed.
 */
@Serializable
data class UserResponse(
    @SerialName("id") val id: Long?,
    @SerialName("username") val username: String,
)