package com.yaroslavzghoba.utils

import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import kotlinx.coroutines.delay
import java.io.File

/**
 * Generate the [tokensAmount] number of JWT tokens with the highest permissions and
 * save them to the [filename] file at the root of the project directory using Markdown syntax.
 *
 * @param tokensAmount Number of tokens to be generated.
 * @param jwtTokenConfig Configuration of strong JWT tokens. The function does not modify the configuration,
 * it can only expand the list with the necessary claims.
 * @param jwtTokenService Service for generating JWT tokens.
 * @param delayBetweenGenerationsMs Delay between token generation in milliseconds.
 * @param filename File name where the keys will be saved.
 */
suspend fun generateAndSaveStrongTokens(
    tokensAmount: Int,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    delayBetweenGenerationsMs: Long = 1000L,
    filename: String = "tokens.md",
) {
    val strongTokens = generateStrongTokens(
        tokensAmount = tokensAmount,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        delayBetweenGenerationsMs = delayBetweenGenerationsMs,
    )
    saveStrongTokensToFile(
        strongTokens = strongTokens,
        filename = filename,
    )
}

private suspend fun generateStrongTokens(
    tokensAmount: Int,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    delayBetweenGenerationsMs: Long = 1000L,
): List<String> {
    return List(tokensAmount) {
        val claims = jwtTokenConfig.claims.toMutableList().apply {
            removeIf { it.key in listOf(Constants.STRONG_TOKEN_CLAIM_KEY, Constants.OWNER_TOKEN_CLAIM_KEY) }
            add(JwtTokenClaim(key = Constants.STRONG_TOKEN_CLAIM_KEY, value = true))
        }
        val config = jwtTokenConfig.copy(claims = claims)
        jwtTokenService.generate(config = config).also {
            delay(delayBetweenGenerationsMs)
        }
    }
}

private fun saveStrongTokensToFile(strongTokens: List<String>, filename: String) {
    val content = strongTokens
        .mapIndexed { index, token -> "${index + 1}. $token" }
        .joinToString(
            separator = "\n",
            prefix = "# Strong Access Tokens\n\n",
        )
    File(filename).writeText(content)
}
