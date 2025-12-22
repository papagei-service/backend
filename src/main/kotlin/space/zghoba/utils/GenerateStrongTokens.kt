package space.zghoba.utils

import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import space.zghoba.security.jwt.JwtTokenClaim
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenService
import java.io.File

private val LOGGER = KtorSimpleLogger("space.zghoba.utils")

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

    val usePluralForm = tokensAmount == 0 || 1 < tokensAmount
    val message = "$tokensAmount strong ${if (usePluralForm) "tokens were" else "token was"} generated " +
            "and saved to the “${filename}” file."
    LOGGER.info(message)
}

private suspend inline fun generateStrongTokens(
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

@Suppress("nothing_to_inline")
private inline fun saveStrongTokensToFile(strongTokens: List<String>, filename: String) {
    val content = strongTokens
        .mapIndexed { index, token -> "${index + 1}. $token" }
        .joinToString(
            separator = "\n",
            prefix = "# \uD83D\uDD11 Strong Access Tokens\n\n",
        )
    File(filename).writeText(content)
}
