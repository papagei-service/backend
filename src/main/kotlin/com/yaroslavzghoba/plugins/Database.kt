package com.yaroslavzghoba.plugins

import com.yaroslavzghoba.utils.DatabaseConfig
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

@Suppress("UnusedReceiverParameter")
fun Application.configureDatabase(databaseConfig: DatabaseConfig) {
    Database.connect(
        url = databaseConfig.url,
        user = databaseConfig.user,
        password = databaseConfig.password,
    )
}