package com.yaroslavzghoba.routing.api

import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.utils.nextChar
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun RouteHandlersProvider.Api.postRegister(
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
): suspend RoutingContext.() -> Unit = postRegisterHandler@{

    // Generate a JWT token
    val client = CharArray(32) { Random.nextChar() }.joinToString(separator = "")
    val clientClaim = JwtTokenClaim(name = "client", value = client)
    val token = jwtTokenService.generate(
        config = jwtTokenConfig.copy(claims = jwtTokenConfig.claims + clientClaim)
    )

    val message = mapOf("token" to token)
    call.respond(status = HttpStatusCode.OK, message = message)
}