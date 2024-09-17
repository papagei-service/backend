package com.yaroslavzghoba

import com.yaroslavzghoba.model.InputCredentials
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

private const val COOKIE_REQUEST_PARAM_NAME = "Cookie"
private const val COOKIE_RESPONCE_PARAM_NAME = "Set-Cookie"

class SessionAuthenticationTest {

    @Test
    fun `Do not grant access if the user is not authenticated`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/users/logout")
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not login with a non-existent username`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)

            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
            setBody(inputCredentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not register with a blank username`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)

            val inputCredentials = InputCredentials(username = " ", password = "qwerty")
            setBody(inputCredentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Do not register with a blank password`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)

            val inputCredentials = InputCredentials(username = "admin", password = " ")
            setBody(inputCredentials)
        }
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Register a new user`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)

            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
            setBody(inputCredentials)
        }
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Do not login with an incorrect password`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Login the existing user with modified password
        val response = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials.copy(password = "password"))
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Login with a correct password`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Login the existing user
        val response = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Grant access to an authenticated user`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Login the existing user & extract the JWT token
        val response0 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]

        // Get access to the protected resource
        val response1 = client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
        }
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not grant access after logout`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Login the existing user & extract the JWT token
        val response0 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]

        // Get access to the protected resource
        client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
        }
        val responce1 = client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = responce1.status,
        )
    }
}