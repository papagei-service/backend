package com.yaroslavzghoba.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yaroslavzghoba.utils.Constants
import io.ktor.util.logging.*
import java.time.Instant
import java.util.*

private val LOGGER = KtorSimpleLogger(JwtTokenServiceImpl::class.java.name)

class JwtTokenServiceImpl : JwtTokenService {

    override fun generate(config: JwtTokenConfig): String {
        val expiresAt = config.lifetimeMs?.let { Date(System.currentTimeMillis() + it) }

        val isStrong = config.claims
            .firstOrNull { it.key == Constants.STRONG_TOKEN_CLAIM_KEY }
            ?.value.toString()
            .toBooleanStrictOrNull() ?: false
        val message = "New${if (isStrong) " " else "not "}strong token requested. " +
                if (expiresAt == null) "Expiration date not specified." else "Expires at: $expiresAt."
        LOGGER.debug(message)

        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .apply { config.claims.forEach { withClaim(it.key, it.value.toString()) } }
            .apply { config.lifetimeMs?.let { withExpiresAt(expiresAt) } }
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256(config.secret))
    }
}