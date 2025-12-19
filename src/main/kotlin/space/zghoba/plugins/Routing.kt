package space.zghoba.plugins

import space.zghoba.domain.HandleCardAnswerUseCase
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.routing.v1.account.*
import space.zghoba.routing.v1.cards.*
import space.zghoba.routing.v1.cards.examples.getExamples
import space.zghoba.routing.v1.cards.examples.postExample
import space.zghoba.routing.v1.collections.*
import space.zghoba.routing.v1.collections.cards.deleteCard
import space.zghoba.routing.v1.collections.cards.postCard
import space.zghoba.routing.v1.examples.deleteExample
import space.zghoba.routing.v1.examples.getExampleById
import space.zghoba.routing.v1.examples.putExample
import space.zghoba.routing.v1.getToken
import space.zghoba.security.hashing.HashingService
import space.zghoba.security.hashing.PasswordSaltConfig
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenService
import space.zghoba.utils.KeyGenerator
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.SwaggerConfig
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*
import io.ktor.server.sessions.SessionStorage
import io.ktor.server.websocket.*

fun Application.configureRouting(
    repository: Repository,
    sessionStorage: SessionStorage,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    keyGenerator: KeyGenerator,
) {
    routing {
        swaggerUI("/docs", "/help", swaggerFile = "openapi/documentation.yaml")
        route(path = "/v1") {
            authenticate("jwt-authentication-v1", strategy = AuthenticationStrategy.Required) {
                handleRoutingV1(
                    repository = repository,
                    sessionStorage = sessionStorage,
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
    sessionStorage: SessionStorage,
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

/**
 * Generate Swagger documentation for each passed path.
 */
private fun Routing.swaggerUI(
    vararg paths: String,
    swaggerFile: String = "openapi/documentation.yaml",
    block: SwaggerConfig.() -> Unit = {},
) {
    paths.forEach { path ->
        swaggerUI(path = path, swaggerFile = swaggerFile, block = block)
    }
}