package com.yaroslavzghoba

import com.yaroslavzghoba.data.FakeUserStorage
import com.yaroslavzghoba.data.UserSessionStorage
import com.yaroslavzghoba.plugins.configureAuthentication
import com.yaroslavzghoba.plugins.configureRouting
import com.yaroslavzghoba.plugins.configureSerialization
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenServiceImpl
import com.yaroslavzghoba.security.sessions.SessionsConfig
import com.yaroslavzghoba.utils.KeyGeneratorImpl
import io.ktor.server.application.*

@Suppress("unused")  // Mark the IDE that the function is actually used
fun Application.testingModule() {
    val userStorage = FakeUserStorage()
    val jwtTokenConfig = JwtTokenConfig(
        secret = environment.config.property("security.jwt.secret").getString(),
        issuer = environment.config.property("security.jwt.issuer").getString(),
        audience = environment.config.property("security.jwt.audience").getString(),
        realm = environment.config.property("security.jwt.realm").getString(),
        lifetimeMs = environment.config.property("security.jwt.lifetime-ms").getString().toLong(),
    )
    val jwtTokenService = JwtTokenServiceImpl()
    val sessionsConfig = SessionsConfig(
        sessionStorage = UserSessionStorage(),
        lifetimeMs = environment.config.property("security.sessions.lifetime-ms").getString().toLong(),
    )
    val sessionStorage = UserSessionStorage()
    val hashingService = HashingServiceImpl(
        pepper = environment.config.property("security.hashing.pepper").getString(),
        algorithm = environment.config.property("security.hashing.algorithm").getString(),
    )
    val saltConfig = PasswordSaltConfig(
        minLength = environment.config.property("security.hashing.salt-min-length").getString().toInt(),
        maxLength = environment.config.property("security.hashing.salt-max-length").getString().toInt(),
    )
    val keyGenerator = KeyGeneratorImpl()

    configureAuthentication(
        jwtTokenConfig = jwtTokenConfig,
        sessionsConfig = sessionsConfig,
        sessionStorage = sessionStorage,
    )
    configureRouting(
        userStorage = userStorage,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        hashingService = hashingService,
        saltConfig = saltConfig,
        keyGenerator = keyGenerator,
    )
    configureSerialization()
}