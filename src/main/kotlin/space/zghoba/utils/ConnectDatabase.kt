package space.zghoba.utils

import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * Connect to the application's remote database.
 *
 * @param dbConnectionConfig Connection configuration. Includes database address, username, password, and more.
 */
fun connectDatabase(dbConnectionConfig: DbConnectionConfig) {
    Database.connect(
        url = with(dbConnectionConfig) { "$driver://$host:$port/$name" },
        user = dbConnectionConfig.user,
        password = dbConnectionConfig.password,
        databaseConfig = DatabaseConfig {
            sqlLogger = StdOutSqlLogger
        },
    )
}