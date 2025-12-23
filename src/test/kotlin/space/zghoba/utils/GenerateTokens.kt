package space.zghoba.utils

import io.ktor.util.logging.*
import space.zghoba.security.jwt.JwtTokenClaim
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenService

private val LOGGER = KtorSimpleLogger(::generateTokens.javaClass.packageName)

@Suppress("unused")
fun generateTokens(
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
) {
    val tokens = listOf(true, false).associateWith { strong ->
        generateToken(
            strong = strong,
            jwtTokenConfig = jwtTokenConfig,
            jwtTokenService = jwtTokenService,
        )
    }

    LOGGER.info("Strong token: ${tokens[true]}")
    LOGGER.info("Not strong token: ${tokens[false]}")
}

private fun generateToken(
    strong: Boolean,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
): String {
    val claims = jwtTokenConfig.claims.toMutableList().apply {
        removeIf { it.key in listOf(Constants.STRONG_TOKEN_CLAIM_KEY, Constants.OWNER_TOKEN_CLAIM_KEY) }
        add(JwtTokenClaim(key = Constants.STRONG_TOKEN_CLAIM_KEY, value = strong))
    }
    val config = jwtTokenConfig.copy(claims = claims)
    return jwtTokenService.generate(config = config)
}