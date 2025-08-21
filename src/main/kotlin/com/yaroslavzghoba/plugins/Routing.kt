package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.domain.HandleCardAnswerUseCase
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.routing.v1.account.*
import com.yaroslavzghoba.routing.v1.cards.*
import com.yaroslavzghoba.routing.v1.cards.examples.getExamples
import com.yaroslavzghoba.routing.v1.cards.examples.postExample
import com.yaroslavzghoba.routing.v1.collections.*
import com.yaroslavzghoba.routing.v1.collections.cards.deleteCard
import com.yaroslavzghoba.routing.v1.collections.cards.postCard
import com.yaroslavzghoba.routing.v1.examples.deleteExample
import com.yaroslavzghoba.routing.v1.examples.getExampleById
import com.yaroslavzghoba.routing.v1.examples.putExample
import com.yaroslavzghoba.routing.v1.getToken
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import com.yaroslavzghoba.utils.KeyGenerator
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureRouting(
    repository: Repository,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    keyGenerator: KeyGenerator,
) {
    routing {
        route(path = "/v1") {
            authenticate("jwt-authentication-v1", strategy = AuthenticationStrategy.Required) {
                handleRoutingV1(
                    repository = repository,
                    jwtTokenConfig = jwtTokenConfig,
                    jwtTokenService = jwtTokenService,
                    hashingService = hashingService,
                    saltConfig = saltConfig,
                    saltGenerator = keyGenerator,
                )
            }
        }
    }
}

private fun Route.handleRoutingV1(
    repository: Repository,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    saltGenerator: KeyGenerator,
) {
    // Register a new, not strong, access token
    authenticate("strong-jwt-authentication-v1", strategy = AuthenticationStrategy.Required) {
        authenticate("session-authentication-v1", strategy = AuthenticationStrategy.Optional) {
            get(
                path = "/token",
                body = RouteHandlersProvider.getToken(
                    jwtTokenConfig = jwtTokenConfig,
                    jwtTokenService = jwtTokenService,
                )
            )
        }
    }

    route(path = "/account") {
        authenticate("session-authentication-v1", strategy = AuthenticationStrategy.Required) {
            get(body = RouteHandlersProvider.V1.Account.getAccount(repository = repository))
        }
        post(
            path = "/login",
            body = RouteHandlersProvider.V1.Account.postLogin(
                repository = repository,
                hashingService = hashingService,
            ),
        )
        post(
            path = "/register",
            body = RouteHandlersProvider.V1.Account.postRegister(
                repository = repository,
                hashingService = hashingService,
                saltConfig = saltConfig,
                saltGenerator = saltGenerator,
            ),
        )
        authenticate("session-authentication-v1", strategy = AuthenticationStrategy.Required) {
            post(
                path = "/logout",
                body = RouteHandlersProvider.V1.Account.postLogout(),
            )

            authenticate("strong-jwt-authentication-v1", strategy = AuthenticationStrategy.Required) {
                delete(body = RouteHandlersProvider.V1.Account.deleteAccount(repository = repository))
            }
        }
    }

    authenticate("session-authentication-v1", strategy = AuthenticationStrategy.Required) {
        route(path = "/collections") {
            get(body = RouteHandlersProvider.V1.Collections.getCollections(repository = repository))
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
                path = "/{collection_id}",
                body = RouteHandlersProvider.V1.Collections
                    .putCollection(repository = repository)
            )
            delete(
                path = "/{collection_id}",
                body = RouteHandlersProvider.V1.Collections
                    .deleteCollection(repository = repository)
            )

            route(path = "/{collection_id}/cards/{card_id}") {
                post(
                    body = RouteHandlersProvider.V1.Collections.Cards
                        .postCard(repository = repository)
                )
                delete(
                    body = RouteHandlersProvider.V1.Collections.Cards
                        .deleteCard(repository = repository)
                )
            }
        }

        route(path = "/cards") {
            webSocket(
                handler = RouteHandlersProvider.V1.Cards.webSocketCards(
                    handleCardAnswerUseCase = HandleCardAnswerUseCase(),
                    repository = repository,
                )
            )
            get(body = RouteHandlersProvider.V1.Cards.getCards(repository = repository))
            get(
                path = "/{card_id}",
                body = RouteHandlersProvider.V1.Cards
                    .getCardById(repository = repository)
            )
            post(
                path = "/",
                body = RouteHandlersProvider.V1.Cards
                    .postCard(repository = repository)
            )
            put(
                path = "/{card_id}",
                body = RouteHandlersProvider.V1.Cards
                    .putCard(repository = repository)
            )
            delete(
                path = "/{card_id}",
                body = RouteHandlersProvider.V1.Cards
                    .deleteCard(repository = repository)
            )

            route(path = "/{card_id}/examples") {
                get(
                    body = RouteHandlersProvider.V1.Cards.Examples
                        .getExamples(repository = repository)
                )
                post(
                    body = RouteHandlersProvider.V1.Cards.Examples
                        .postExample(repository = repository)
                )
            }
        }

        route(path = "/examples") {
            get(
                path = "/{example_id}",
                body = RouteHandlersProvider.V1.Examples.getExampleById(repository = repository),
            )
            put(
                path = "/{example_id}",
                body = RouteHandlersProvider.V1.Examples.putExample(repository = repository),
            )
            delete(
                path = "/{example_id}",
                body = RouteHandlersProvider.V1.Examples.deleteExample(repository = repository),
            )
        }
    }
}