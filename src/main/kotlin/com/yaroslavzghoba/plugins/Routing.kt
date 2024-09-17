package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.routing.api.v1.users.postLogin
import com.yaroslavzghoba.routing.api.v1.users.postLogout
import com.yaroslavzghoba.routing.api.v1.users.postRegister
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.SaltGenerator
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userStorage: UserStorage,
    hashingService: HashingService,
    saltGenerator: SaltGenerator,
) {
    routing {
        route(path = "/api") {
            routingApiV1(
                userStorage = userStorage,
                hashingService = hashingService,
                saltGenerator = saltGenerator,
            )
        }
    }
}

private fun Route.routingApiV1(
    userStorage: UserStorage,
    hashingService: HashingService,
    saltGenerator: SaltGenerator,
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
                    saltGenerator = saltGenerator,
                ),
            )
            authenticate("session-authentication") {
                post(
                    path = "/logout",
                    body = RouteHandlersProvider.Api.V1.Users.postLogout(),
                )
            }
        }
    }
}