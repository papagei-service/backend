package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the credentials sent by the client for registration.
 *
 * @param username A unique identifier of the user's account.
 * @param displayName A username, but not unique. Intended for display purposes.
 * @param password A special code word that used to log in the system.
 */
@Serializable
data class RegistrationCredentials(
    @SerialName("username") val username: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("password") val password: String,
)