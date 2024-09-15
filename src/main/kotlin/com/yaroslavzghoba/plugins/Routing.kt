package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.routing.api.v1.getRoot
import com.yaroslavzghoba.routing.api.v1.postSignIn
import com.yaroslavzghoba.routing.api.v1.postSignUp
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.SaltGenerator
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userStorage: UserStorage,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltGenerator: SaltGenerator,
    pepper: String,
) {
    routing {
        route(path = "/api") {
            route(path = "/v1") {
                authenticate("jwt-authentication") {
                    get(path = "/", body = RouteHandlersProvider.Api.V1.getRoot)
                }
                post(
                    path = "/sign-in",
                    body = {
                        RouteHandlersProvider.Api.V1
                            .postSignIn(this, userStorage, hashingService, jwtTokenConfig, jwtTokenService)
                    },
                )
                post(
                    path = "/sign-up",
                    body = {
                        RouteHandlersProvider.Api.V1
                            .postSignUp(this, userStorage, hashingService, saltGenerator, pepper)
                    }
                )
            }
        }
    }
}