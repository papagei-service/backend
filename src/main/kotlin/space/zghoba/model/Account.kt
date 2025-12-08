package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represent an account of a user on the service.
 *
 * @param id A unique identifier of the user's account that cannot be changed.
 * @param displayName A username, but not unique. Intended for display purposes.
 * @param username A unique identifier of the user's account that can be changed.
 */
@Serializable
data class Account(
    @SerialName("id") val id: Long?,
    @SerialName("display_name") val displayName: String,
    @SerialName("username") val username: String,
)