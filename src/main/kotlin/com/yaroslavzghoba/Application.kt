package com.yaroslavzghoba

import com.yaroslavzghoba.data.RepositoryImpl
import com.yaroslavzghoba.data.local.CardStorageImpl
import com.yaroslavzghoba.data.local.CollectionStorageImpl
import com.yaroslavzghoba.data.local.UserSessionStorage
import com.yaroslavzghoba.data.local.UserStorageImpl
import com.yaroslavzghoba.plugins.configureAuthentication
import com.yaroslavzghoba.plugins.configureDatabase
import com.yaroslavzghoba.plugins.configureRouting
import com.yaroslavzghoba.plugins.configureSerialization
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenServiceImpl
import com.yaroslavzghoba.security.sessions.SessionsConfig
import com.yaroslavzghoba.utils.DbConnectionConfig
import com.yaroslavzghoba.utils.KeyGeneratorImpl
import com.yaroslavzghoba.utils.generateAndSaveStrongTokens
import io.ktor.server.application.*
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")  // Mark the IDE that the function is actually used
fun Application.module() {
    val repository = RepositoryImpl(
        userStorage = UserStorageImpl(),
        collectionStorage = CollectionStorageImpl(),
        cardStorage = CardStorageImpl(),
    )
    val jwtTokenConfig = JwtTokenConfig(
        secret = environment.config.property("security.jwt.secret").getString(),
        issuer = environment.config.property("security.jwt.issuer").getString(),
        audience = environment.config.property("security.jwt.audience").getString(),
        realm = environment.config.property("security.jwt.realm").getString(),
        lifetimeMs = null,
    )
    val jwtTokenService = JwtTokenServiceImpl()
    val sessionsConfig = SessionsConfig(
        sessionStorage = UserSessionStorage(),
        lifetimeMs = environment.config.property("security.sessions.lifetime-ms").getString().toLong(),
    )
    val hashingService = HashingServiceImpl(
        pepper = environment.config.property("security.hashing.pepper").getString(),
        algorithm = environment.config.property("security.hashing.algorithm").getString(),
    )
    val saltConfig = PasswordSaltConfig(
        minLength = environment.config.property("security.hashing.salt-min-length").getString().toInt(),
        maxLength = environment.config.property("security.hashing.salt-max-length").getString().toInt(),
    )
    val keyGenerator = KeyGeneratorImpl()
    val dbConnectionConfig = DbConnectionConfig(
        url = environment.config.property("database.url").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString(),
    )

    // Generate strong tokens and save them in a file
    launch {
        generateAndSaveStrongTokens(
            tokensAmount = 1,
            jwtTokenConfig = jwtTokenConfig,
            jwtTokenService = jwtTokenService,
        )
    }

    configureAuthentication(
        jwtTokenConfig = jwtTokenConfig,
        sessionsConfig = sessionsConfig,
    )
    configureRouting(
        repository = repository,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        hashingService = hashingService,
        saltConfig = saltConfig,
        keyGenerator = keyGenerator,
    )
    configureSerialization()
    configureDatabase(dbConnectionConfig = dbConnectionConfig)
}
