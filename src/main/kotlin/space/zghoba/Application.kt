package space.zghoba

import io.ktor.server.application.*
import space.zghoba.data.RepositoryImpl
import space.zghoba.data.local.*
import space.zghoba.plugins.*
import space.zghoba.security.hashing.HashingServiceImpl
import space.zghoba.security.hashing.PasswordSaltConfig
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenServiceImpl
import space.zghoba.security.sessions.SessionsConfig
import space.zghoba.utils.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")  // Mark the IDE that the function is actually used
suspend fun Application.module() {
    val repository = RepositoryImpl(
        userStorage = UserStorageImpl(),
        collectionStorage = CollectionStorageImpl(),
        cardStorage = CardStorageImpl(),
        exampleStorage = ExampleStorageImpl(),
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
        driver = environment.config.property("database.driver").getString(),
        host = environment.config.property("database.host").getString(),
        port = environment.config.property("database.port").getString().toInt(),
        name = environment.config.property("database.name").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString(),
    )
    val dbMigrationScriptsDirectoryPath =
        environment.config.property("database.migrations-dir-path").getString()

    configureAuthentication(
        jwtTokenConfig = jwtTokenConfig,
        sessionsConfig = sessionsConfig,
    )
    configureWebSockets()
    configureRouting(
        repository = repository,
        sessionStorage = sessionsConfig.sessionStorage,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        hashingService = hashingService,
        saltConfig = saltConfig,
        keyGenerator = keyGenerator,
    )
    configureSerialization()
    configureStatusPages()

    connectDatabase(dbConnectionConfig = dbConnectionConfig)
    executeDbSchemaMigrations(scriptsDirectoryPath = dbMigrationScriptsDirectoryPath)
}
