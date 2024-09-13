package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.model.Credentials
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private typealias PostSignInHandler = suspend RoutingContext.(
    userStorage: UserStorage,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
) -> Unit

val RouteHandlersProvider.Api.V1.postSignIn: PostSignInHandler
    get() = postSignIn@{ userStorage, jwtTokenConfig, jwtTokenService ->
        val credentials = call.receive<Credentials>()

        userStorage.getByUsername(username = credentials.username)?.let { user ->
            // Check the user's password
            if (credentials.password == user.password) {
                val usernameClaim = JwtTokenClaim(name = "username", value = credentials.username)
                val token = jwtTokenService.generate(
                    config = jwtTokenConfig.copy(claims = jwtTokenConfig.claims + usernameClaim)
                )
                call.respond(mapOf("token" to token))
                return@postSignIn
            }
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = mapOf("message" to "The password is incorrect"),
            )
            return@postSignIn
        }
        call.respond(
            status = HttpStatusCode.Unauthorized,
            message = mapOf("meesage" to "There is no the user with the '${credentials.username}' username"),
        )
    }