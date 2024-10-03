package com.yaroslavzghoba

import com.yaroslavzghoba.data.RepositoryImpl
import com.yaroslavzghoba.data.local.CardStorageImpl
import com.yaroslavzghoba.data.local.CollectionStorageImpl
import com.yaroslavzghoba.data.local.UserSessionStorage
import com.yaroslavzghoba.data.local.UserStorageImpl
import com.yaroslavzghoba.data.model.PurgeableSessionStorage
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.plugins.configureAuthentication
import com.yaroslavzghoba.plugins.configureDatabase
import com.yaroslavzghoba.plugins.configureRouting
import com.yaroslavzghoba.plugins.configureSerialization
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenServiceImpl
import com.yaroslavzghoba.security.sessions.SessionsConfig
import com.yaroslavzghoba.utils.KeyGeneratorImpl
import io.ktor.server.application.*

private val Repository: Repository = RepositoryImpl(
    userStorage = UserStorageImpl(),
    collectionStorage = CollectionStorageImpl(),
    cardStorage = CardStorageImpl(),
)
private val SessionStorage: PurgeableSessionStorage = UserSessionStorage()

@Suppress("unused")  // Mark the IDE that the function is actually used
fun Application.testingModule() {
    val jwtTokenConfig = JwtTokenConfig(
        secret = environment.config.property("security.jwt.secret").getString(),
        issuer = environment.config.property("security.jwt.issuer").getString(),
        audience = environment.config.property("security.jwt.audience").getString(),
        realm = environment.config.property("security.jwt.realm").getString(),
        lifetimeMs = environment.config.property("security.jwt.lifetime-ms").getString().toLong(),
    )
    val jwtTokenService = JwtTokenServiceImpl()
    val sessionsConfig = SessionsConfig(
        sessionStorage = SessionStorage,
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
    val dbConnectionConfig = com.yaroslavzghoba.utils.DbConnectionConfig(
        url = environment.config.property("database.url").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString(),
    )

    configureAuthentication(
        jwtTokenConfig = jwtTokenConfig,
        sessionsConfig = sessionsConfig,
    )
    configureRouting(
        repository = Repository,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        hashingService = hashingService,
        saltConfig = saltConfig,
        keyGenerator = keyGenerator,
    )
    configureSerialization()
    configureDatabase(dbConnectionConfig = dbConnectionConfig)
}

/**
 * Delete all rows in all database tables.
 */
suspend fun clearDatabase() {
    Repository.clear()
    SessionStorage.invalidateAll()
}