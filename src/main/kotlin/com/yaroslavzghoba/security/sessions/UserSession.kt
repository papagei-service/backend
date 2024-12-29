package com.yaroslavzghoba.security.sessions

import kotlinx.serialization.Serializable

/**
 * Represents a separate user session, which is a user identifier for each HTTP request.
 *
 * @param userId Unique immutable identifier of the user.
 */
@Serializable
data class UserSession(val userId: Long)