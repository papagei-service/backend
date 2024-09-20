package com.yaroslavzghoba.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.*

class JwtTokenServiceImpl : JwtTokenService {

    override fun generate(config: JwtTokenConfig): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .apply { config.claims.forEach { withClaim(it.key, it.value.toString()) } }
            .apply { config.lifetimeMs?.let { withExpiresAt(Date(System.currentTimeMillis() + it)) } }
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256(config.secret))
    }
}