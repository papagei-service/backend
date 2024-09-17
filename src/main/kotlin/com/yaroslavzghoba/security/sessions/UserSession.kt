package com.yaroslavzghoba.security.sessions

import kotlinx.serialization.Serializable

/**
 * Represents a separate user session, which is a user identifier for each HTTP request.
 *
 * @param username Unique string user identifier.
 */
@Serializable
data class UserSession(val username: String)