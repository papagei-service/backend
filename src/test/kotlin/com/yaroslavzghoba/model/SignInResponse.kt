package com.yaroslavzghoba.model

import kotlinx.serialization.Serializable

/**
 * Represents a successful response to the sign in request.
 *
 * @param token A JWT token for further access to protected resources.
 */
@Serializable
data class SignInResponse(
    val token: String,
)