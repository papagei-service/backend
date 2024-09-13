package com.yaroslavzghoba

import com.yaroslavzghoba.data.FakeUserStorage
import com.yaroslavzghoba.plugins.configureAuthentication
import com.yaroslavzghoba.plugins.configureRouting
import com.yaroslavzghoba.plugins.configureSerialization
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenServiceImpl
import com.yaroslavzghoba.utils.Constants
import io.ktor.server.application.*

@Suppress("unused")  // Mark the IDE that the function is actually used
fun Application.testingModule() {
    val userStorage = FakeUserStorage()
    val jwtTokenConfig = JwtTokenConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString(),
        lifetimeMs = Constants.JWT_TOKEN_LIFETIME_MS,
    )
    val jwtTokenService = JwtTokenServiceImpl()

    configureAuthentication(jwtTokenConfig = jwtTokenConfig)
    configureRouting(
        userStorage = userStorage,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
    )
    configureSerialization()
}