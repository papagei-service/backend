package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.routing.api.postRegister
import com.yaroslavzghoba.routing.api.v1.users.postLogin
import com.yaroslavzghoba.routing.api.v1.users.postLogout
import com.yaroslavzghoba.routing.api.v1.users.postRegister
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.utils.KeyGenerator
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userStorage: UserStorage,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    keyGenerator: KeyGenerator,
) {
    routing {
        route(path = "/api") {
            authenticate("jwt-authentication", strategy = AuthenticationStrategy.Required) {
                routingApiV1(
                    userStorage = userStorage,
                    hashingService = hashingService,
                    saltConfig = saltConfig,
                    saltGenerator = keyGenerator,
                )
            }

            // Register a new, not strong, access token
            authenticate("strong-jwt-authentication", strategy = AuthenticationStrategy.Required) {
                authenticate("session-authentication", strategy = AuthenticationStrategy.Optional) {
                    post(
                        path = "/register",
                        body = RouteHandlersProvider.Api.postRegister(
                            jwtTokenConfig = jwtTokenConfig,
                            jwtTokenService = jwtTokenService,
                        )
                    )
                }
            }
        }
    }
}

private fun Route.routingApiV1(
    userStorage: UserStorage,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    saltGenerator: KeyGenerator,
) {
    route(path = "/v1") {
        route(path = "/users") {
            post(
                path = "/login",
                body = RouteHandlersProvider.Api.V1.Users.postLogin(
                    userStorage = userStorage,
                    hashingService = hashingService,
                ),
            )
            post(
                path = "/register",
                body = RouteHandlersProvider.Api.V1.Users.postRegister(
                    userStorage = userStorage,
                    hashingService = hashingService,
                    saltConfig = saltConfig,
                    saltGenerator = saltGenerator,
                ),
            )
            authenticate("session-authentication", strategy = AuthenticationStrategy.Required) {
                post(
                    path = "/logout",
                    body = RouteHandlersProvider.Api.V1.Users.postLogout(),
                )
            }
        }
    }
}