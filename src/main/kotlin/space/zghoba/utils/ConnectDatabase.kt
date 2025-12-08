package space.zghoba.utils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger

/**
 * Connect to the application's remote database.
 *
 * @param dbConnectionConfig Connection configuration. Includes database address, username, password, and more.
 */
fun connectDatabase(dbConnectionConfig: DbConnectionConfig) {
    Database.connect(
        url = dbConnectionConfig.url,
        user = dbConnectionConfig.user,
        password = dbConnectionConfig.password,
        databaseConfig = DatabaseConfig {
            sqlLogger = StdOutSqlLogger
        },
    )
}