package com.yaroslavzghoba.security.jwt

/**
 * Defines the methods required to work with JWT tokens.
 */
interface JwtTokenService {

    /**
     * Generates a new JWT token.
     *
     * @param config A configuration of the JWT token that must be generated.
     * @return A generated JWT token.
     */
    fun generate(config: JwtTokenConfig): String
}