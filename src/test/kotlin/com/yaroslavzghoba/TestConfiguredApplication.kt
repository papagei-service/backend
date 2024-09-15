package com.yaroslavzghoba

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*

fun testConfiguredApplication(
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit,
) = testApplication {

    environment {
        config = ApplicationConfig("application-test.yaml")
    }

    val client = createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    block(client)
}