package com.yaroslavzghoba.utils

import com.yaroslavzghoba.clearTestingDatabase
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun testConfiguredApplication(
    block: suspend ApplicationTestBuilder.(client: HttpClient, applicationConfig: ApplicationConfig) -> Unit,
) = testApplication {

    val applicationConfig = ApplicationConfig("application-test.yaml")

    environment {
        config = applicationConfig
    }

    val client = createClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    startApplication()  // Must be running to access the database

    // Delete all rows in the database to make the tests independent of each other
    // This is equivalent to clearing the database in the method annotated with @BeforeTest (using kotlin-test library).
    clearTestingDatabase()

    // Run a test with a limited execution time
    withTimeout(10.seconds) {
        block(client, applicationConfig)
    }
}