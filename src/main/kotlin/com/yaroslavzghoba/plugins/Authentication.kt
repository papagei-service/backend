package com.yaroslavzghoba.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.sessions.SessionsConfig
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

private const val MILLISECONDS_IN_SECOND: Long = 1000

fun Application.configureAuthentication(
    jwtTokenConfig: JwtTokenConfig,
    sessionsConfig: SessionsConfig,
    sessionStorage: SessionStorage,
) {
    val jwtAuthConfiguration = jwtAuthConfiguration(jwtTokenConfig = jwtTokenConfig)
    val strongJwtAuthConfiguration = strongJwtAuthConfiguration(jwtTokenConfig = jwtTokenConfig)
    val sessionsConfiguration = sessionsConfiguration<UserSession>(
        sessionsConfig = sessionsConfig,
        sessionStorage = sessionStorage,
    )
    val sessionAuthConfiguration = sessionAuthConfiguration<UserSession>()

    install(plugin = Sessions, configure = sessionsConfiguration)
    install(plugin = Authentication) {
        jwt(name = "jwt-authentication", configure = jwtAuthConfiguration)
        jwt(name = "strong-jwt-authentication", configure = strongJwtAuthConfiguration)
        session(name = "session-authentication", configure = sessionAuthConfiguration)
    }
}

private inline fun <reified T : Any> sessionsConfiguration(
    sessionsConfig: SessionsConfig,
    sessionStorage: SessionStorage,
): io.ktor.server.sessions.SessionsConfig.() -> Unit = {

    cookie<T>(name = "session", storage = sessionStorage) {
        cookie.path = "/"
        cookie.httpOnly = true
        cookie.secure = true
        sessionsConfig.lifetimeMs?.let { lifetimeMs ->
            cookie.maxAgeInSeconds = lifetimeMs * MILLISECONDS_IN_SECOND
        }
    }
}

private fun <T : Any> sessionAuthConfiguration(): SessionAuthenticationProvider.Config<T>.() -> Unit = {
    // Additional validations of the session
    validate { it }

    // Return 401 if session authentication fails
    challenge { _ ->
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
    }
}

private fun jwtAuthConfiguration(
    jwtTokenConfig: JwtTokenConfig,
): JWTAuthenticationProvider.Config.() -> Unit = {

    realm = jwtTokenConfig.realm

    // Additional validations on the JWT payload
    validate { credential ->
        JWTPrincipal(credential.payload)
    }

    // Set a token format and signature verifier
    val verifier = JWT
        .require(Algorithm.HMAC256(jwtTokenConfig.secret))
        .withAudience(jwtTokenConfig.audience)
        .withIssuer(jwtTokenConfig.issuer)
        .build()
    verifier(verifier)

    // Return 401 if JWT authentication fails
    challenge { _, _ ->
        val message = mapOf("message" to "Token is not valid or has expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
    }
}

private fun strongJwtAuthConfiguration(
    jwtTokenConfig: JwtTokenConfig,
): JWTAuthenticationProvider.Config.() -> Unit = {

    realm = jwtTokenConfig.realm

    // Additional validations on the JWT payload
    validate { credential ->
        val isStrong = credential.payload.getClaim("strong").asString().toBooleanStrictOrNull() ?: return@validate null
        if (isStrong) JWTPrincipal(credential.payload) else null
    }

    // Set a token format and signature verifier
    val verifier = JWT
        .require(Algorithm.HMAC256(jwtTokenConfig.secret))
        .withAudience(jwtTokenConfig.audience)
        .withIssuer(jwtTokenConfig.issuer)
        .build()
    verifier(verifier)

    // Return 401 if JWT authentication fails
    challenge { _, _ ->
        val message = mapOf("message" to "Token is not valid or has expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
    }
}