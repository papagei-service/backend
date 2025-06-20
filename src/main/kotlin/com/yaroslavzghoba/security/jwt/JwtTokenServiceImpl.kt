package com.yaroslavzghoba.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.util.logging.*
import java.time.Instant
import java.util.*

internal val LOGGER = KtorSimpleLogger("com.yaroslavzghoba.papagei")

class JwtTokenServiceImpl : JwtTokenService {

    override fun generate(config: JwtTokenConfig): String {
        val expiresAt = config.lifetimeMs?.let { Date(System.currentTimeMillis() + it) }
        LOGGER.debug("JwtTokenServiceImpl: New token requested. Expires at: {}", expiresAt)
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .apply { config.claims.forEach { withClaim(it.key, it.value.toString()) } }
            .apply { config.lifetimeMs?.let { withExpiresAt(expiresAt) } }
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256(config.secret))
    }
}