package space.zghoba

import io.ktor.server.application.*
import io.ktor.server.routing.*
import space.zghoba.data.RepositoryImpl
import space.zghoba.data.local.*
import space.zghoba.data.model.PurgeableSessionStorage
import space.zghoba.model.Repository
import space.zghoba.plugins.*
import space.zghoba.security.hashing.HashingServiceImpl
import space.zghoba.security.hashing.PasswordSaltConfig
import space.zghoba.security.jwt.JwtTokenConfig
import space.zghoba.security.jwt.JwtTokenServiceImpl
import space.zghoba.security.sessions.SessionsConfig
import space.zghoba.utils.DbConnectionConfig
import space.zghoba.utils.KeyGeneratorImpl
import space.zghoba.utils.connectDatabase
import space.zghoba.utils.executeDbSchemaMigrations

private val Repository: Repository = RepositoryImpl(
    userStorage = UserStorageImpl(),
    collectionStorage = CollectionStorageImpl(),
    cardStorage = CardStorageImpl(),
    exampleStorage = ExampleStorageImpl(),
)
private val SessionStorage: PurgeableSessionStorage = UserSessionStorage()

@Suppress("unused")  // Mark the IDE that the function is actually used
suspend fun Application.testingModule() {
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
        repository = Repository,
        sessionStorage = SessionStorage,
        jwtTokenConfig = jwtTokenConfig,
        jwtTokenService = jwtTokenService,
        hashingService = hashingService,
        saltConfig = saltConfig,
        keyGenerator = keyGenerator,
    )
    configureSerialization()
    configureStatusPages()

    // Configure the database
    connectDatabase(dbConnectionConfig = dbConnectionConfig)
    executeDbSchemaMigrations(scriptsDirectoryPath = dbMigrationScriptsDirectoryPath)
    clearAllRowsInDatabase()

    // Necessary to test the functionality of the StatusPages plugin
    routing {
        get(path = "/error") {
            throw Exception()
        }
    }
}

/**
 * Delete all rows in all testing database tables.
 */
private suspend fun clearAllRowsInDatabase() {
    Repository.clear()
    SessionStorage.invalidateAll()
}