package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.utils.DbConnectionConfig
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger

@Suppress("UnusedReceiverParameter")
fun Application.configureDatabase(dbConnectionConfig: DbConnectionConfig) {
    Database.connect(
        url = dbConnectionConfig.url,
        user = dbConnectionConfig.user,
        password = dbConnectionConfig.password,
        databaseConfig = DatabaseConfig {
            sqlLogger = StdOutSqlLogger
        },
    )
}