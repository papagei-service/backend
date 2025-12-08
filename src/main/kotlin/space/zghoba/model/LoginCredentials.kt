package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the credentials sent by the client for login.
 *
 * @param username A unique identifier of the user's account.
 * @param password A special code word that used to log in the system.
 */
@Serializable
data class LoginCredentials(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)