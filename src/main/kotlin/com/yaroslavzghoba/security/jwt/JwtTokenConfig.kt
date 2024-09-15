package com.yaroslavzghoba.security.jwt

/**
 * Displays the JWT token configuration signed using the HMAC SHA256 algorithm.
 *
 * @param secret A special 256-bit key used for signing. It must be your small secret.
 * @param issuer Identifies the principal that issued the JWT.
 * @param audience Identifies the recipients that the JWT is intended for.
 * @param realm Used to specify the scope of protection.
 * @param claims Custom claims that are included in the payload. By default, it's an empty list.
 * @param lifetimeMs Token lifetime in milliseconds. If `null`, then it is unlimited.
 */
data class JwtTokenConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val lifetimeMs: Long?,
    val claims: List<JwtTokenClaim> = emptyList(),
)