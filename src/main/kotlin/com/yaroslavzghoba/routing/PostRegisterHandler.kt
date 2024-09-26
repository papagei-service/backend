package com.yaroslavzghoba.routing

import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.security.sessions.UserSession
import com.yaroslavzghoba.utils.Constants
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.postRegister(
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
): suspend RoutingContext.() -> Unit = postRegisterHandler@{

    // Get a user's session if specified
    val username = call.sessions.get<UserSession>()?.username

    // Generate a JWT token
    val claims = jwtTokenConfig.claims.toMutableList().apply {
        removeIf { it.key in listOf(Constants.STRONG_TOKEN_CLAIM_KEY, Constants.OWNER_TOKEN_CLAIM_KEY) }
        add(JwtTokenClaim(key = Constants.STRONG_TOKEN_CLAIM_KEY, value = true))
        if (username != null)
            add(JwtTokenClaim(key = Constants.OWNER_TOKEN_CLAIM_KEY, value = username))
    }
    val token = jwtTokenService.generate(
        config = jwtTokenConfig.copy(claims = claims)
    )

    val message = mapOf("token" to token)
    call.respond(status = HttpStatusCode.OK, message = message)
}