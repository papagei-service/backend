package space.zghoba.mappers

import space.zghoba.model.LoginCredentials
import space.zghoba.model.RegistrationCredentials

/**
 * Converts an instance of the [RegistrationCredentials] class to an instance of the [LoginCredentials] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun RegistrationCredentials.toLoginCredentials() = LoginCredentials(
    username = this.username,
    password = this.password,
)

/**
 * Converts an instance of the [LoginCredentials] class to an instance of the [RegistrationCredentials] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun LoginCredentials.toLoginCredentials(displayName: String) = RegistrationCredentials(
    username = this.username,
    displayName = displayName,
    password = this.password,
)