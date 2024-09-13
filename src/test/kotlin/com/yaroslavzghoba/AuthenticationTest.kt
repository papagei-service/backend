package com.yaroslavzghoba

import com.yaroslavzghoba.model.Credentials
import com.yaroslavzghoba.model.SignInResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticationTest {

    private fun testConfiguredApplication(
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

    @Test
    fun `Do not grant access if the user is not authorized`() = testConfiguredApplication { client ->
        val response = client.get("/api/v1/")
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not sign in with a non-existent username`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-in") {
            header("Content-Type", "application/json")

            val credentials = Credentials(username = "admin", password = "qwerty")
            setBody(credentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not sign up with a blank username`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")

            val credentials = Credentials(username = " ", password = "qwerty")
            setBody(credentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not sign up with a blank password`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")

            val credentials = Credentials(username = "admin", password = " ")
            setBody(credentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Sign up a new user`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")

            val credentials = Credentials(username = "admin", password = "qwerty")
            setBody(credentials)
        }
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Do not sign in with an incorrect password`() = testConfiguredApplication { client ->
        val credentials = Credentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")
            setBody(credentials)
        }

        // Sign in the existing user
        val response = client.post("/api/v1/sign-in") {
            header("Content-Type", "application/json")
            setBody(credentials.copy(password = "password"))
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Sign in with a correct password`() = testConfiguredApplication { client ->
        val credentials = Credentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")
            setBody(credentials)
        }

        // Sign in the existing user
        val response = client.post("/api/v1/sign-in") {
            header("Content-Type", "application/json")
            setBody(credentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Grant access to an authorized user`() = testConfiguredApplication { client ->
        val credentials = Credentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            header("Content-Type", "application/json")
            setBody(credentials)
        }

        // Sign in the existing user & extract the JWT token
        val response0 = client.post("/api/v1/sign-in") {
            header("Content-Type", "application/json")
            setBody(credentials)
        }
        val jwtToken = response0.body<SignInResponse>().token

        // Get access to the protected resource
        val response1 = client.get("/api/v1/") {
            header("Authorization", "Bearer $jwtToken")
        }
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response1.status,
        )
    }
}