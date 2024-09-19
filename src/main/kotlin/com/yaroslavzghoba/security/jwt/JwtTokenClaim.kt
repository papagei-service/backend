package com.yaroslavzghoba.security.jwt

/**
 * It is a claim that is placed in the JWT token payload.
 *
 * @param key The key used to access the [value].
 * @param value Payload of the claim.
 */
data class JwtTokenClaim(
    val key: String,
    val value: Any,
)