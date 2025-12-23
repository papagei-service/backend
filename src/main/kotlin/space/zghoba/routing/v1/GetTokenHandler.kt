package space.zghoba.routing.v1

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.jwt.JwtTokenClaim
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenService
import space.zghoba.security.sessions.UserSession
import space.zghoba.utils.Constants

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.getToken(
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
): suspend RoutingContext.() -> Unit = getTokenHandler@{

    // Get a user's session if specified
    val session = call.sessions.get<UserSession>()

    // Generate a JWT token
    val claims = jwtTokenConfig.claims.toMutableList().apply {
        removeIf { it.key in listOf(Constants.STRONG_TOKEN_CLAIM_KEY, Constants.OWNER_TOKEN_CLAIM_KEY) }
        add(JwtTokenClaim(key = Constants.STRONG_TOKEN_CLAIM_KEY, value = false))
        if (session != null)
            add(JwtTokenClaim(key = Constants.OWNER_TOKEN_CLAIM_KEY, value = session.userId))
    }
    val token = jwtTokenService.generate(
        config = jwtTokenConfig.copy(claims = claims)
    )

    val message = mapOf("token" to token)
    call.respond(status = HttpStatusCode.OK, message = message)
}