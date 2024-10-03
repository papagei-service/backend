package com.yaroslavzghoba

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*

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
    }

    startApplication()  // Must be running to access the database

    // Delete all rows in the database to make the tests independent of each other
    // This is equivalent to clearing the database in the method annotated with @BeforeTest (using kotlin-test library).
    clearDatabase()

    block(client, applicationConfig)
}