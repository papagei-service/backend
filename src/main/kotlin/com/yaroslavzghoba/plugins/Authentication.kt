package com.yaroslavzghoba.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
//import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication(jwtTokenConfig: JwtTokenConfig) {
    install(Authentication) {
        jwt("jwt-authentication") {
            realm = jwtTokenConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtTokenConfig.secret))
                    .withAudience(jwtTokenConfig.audience)
                    .withIssuer(jwtTokenConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = mapOf(
                        "message" to "Token is not valid or has expired",
                        "defaultScheme" to defaultScheme,
                        "realm" to realm,
                    ),
                )
            }
        }
    }
}