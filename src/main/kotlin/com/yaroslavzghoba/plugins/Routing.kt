package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.routing.postRegister
import com.yaroslavzghoba.routing.v1.collections.*
import com.yaroslavzghoba.routing.v1.collections.cards.deleteCard
import com.yaroslavzghoba.routing.v1.collections.cards.getCardById
import com.yaroslavzghoba.routing.v1.collections.cards.postCard
import com.yaroslavzghoba.routing.v1.collections.cards.putCard
import com.yaroslavzghoba.routing.v1.getCollections
import com.yaroslavzghoba.routing.v1.users.*
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.utils.KeyGenerator
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    repository: Repository,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    keyGenerator: KeyGenerator,
) {
    routing {
        authenticate("jwt-authentication", strategy = AuthenticationStrategy.Required) {
            routingApiV1(
                repository = repository,
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
                    body = RouteHandlersProvider.postRegister(
                        jwtTokenConfig = jwtTokenConfig,
                        jwtTokenService = jwtTokenService,
                    )
                )
            }
        }
    }
}

private fun Route.routingApiV1(
    repository: Repository,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    saltGenerator: KeyGenerator,
) {
    route(path = "/v1") {
        route(path = "/users") {
            authenticate("session-authentication", strategy = AuthenticationStrategy.Required) {
                get(
                    path = "/",
                    body = RouteHandlersProvider.V1.Users.getUser(repository = repository)
                )
            }
            post(
                path = "/login",
                body = RouteHandlersProvider.V1.Users.postLogin(
                    repository = repository,
                    hashingService = hashingService,
                ),
            )
            post(
                path = "/register",
                body = RouteHandlersProvider.V1.Users.postRegister(
                    repository = repository,
                    hashingService = hashingService,
                    saltConfig = saltConfig,
                    saltGenerator = saltGenerator,
                ),
            )
            authenticate("session-authentication", strategy = AuthenticationStrategy.Required) {
                post(
                    path = "/logout",
                    body = RouteHandlersProvider.V1.Users.postLogout(),
                )

                authenticate("strong-jwt-authentication", strategy = AuthenticationStrategy.Required) {
                    delete(
                        path = "/",
                        body = RouteHandlersProvider.V1.Users
                            .deleteUser(repository = repository)
                    )
                }
            }
        }
        authenticate("session-authentication", strategy = AuthenticationStrategy.Required) {
            route(path = "/collections") {
                get(body = RouteHandlersProvider.V1.getCollections(repository = repository))
                get(
                    path = "/{collection_id}",
                    body = RouteHandlersProvider.V1.Collections
                        .getCollectionById(repository = repository)
                )
                post(
                    path = "/",
                    body = RouteHandlersProvider.V1.Collections
                        .postCollection(repository = repository)
                )
                put(
                    path = "/",
                    body = RouteHandlersProvider.V1.Collections
                        .putCollection(repository = repository)
                )
                delete(
                    path = "/{collection_id}",
                    body = RouteHandlersProvider.V1.Collections
                        .deleteCollection(repository = repository)
                )
                route(path = "/{collection_id}/cards") {
                    get(
                        body = RouteHandlersProvider.V1.Collections.Cards
                            .getCollectionCards(repository = repository)
                    )
                    get(
                        path = "/{card_id}",
                        body = RouteHandlersProvider.V1.Collections.Cards
                            .getCardById(repository = repository)
                    )
                    post(
                        path = "/",
                        body = RouteHandlersProvider.V1.Collections.Cards
                            .postCard(repository = repository)
                    )
                    put(
                        path = "/",
                        body = RouteHandlersProvider.V1.Collections.Cards
                            .putCard(repository = repository)
                    )
                    delete(
                        path = "/{card_id}",
                        body = RouteHandlersProvider.V1.Collections.Cards
                            .deleteCard(repository = repository)
                    )
                }
            }
        }
    }
}