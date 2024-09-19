package com.yaroslavzghoba.routing.api

import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.utils.KeyGenerator
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RouteHandlersProvider.Api.postRegister(
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    keyGenerator: KeyGenerator,
): suspend RoutingContext.() -> Unit = postRegisterHandler@{

    // Generate a JWT token
    val clientIdLength = (32..48).random()
    val clientId = keyGenerator.generate(length = clientIdLength)
    val claims = jwtTokenConfig.claims
        .plus(JwtTokenClaim(key = "client_id", value = clientId))
    val token = jwtTokenService.generate(
        config = jwtTokenConfig.copy(claims = claims)
    )

    val message = mapOf("token" to token)
    call.respond(status = HttpStatusCode.OK, message = message)
}