package com.yaroslavzghoba

import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.TokenRegistrationResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

private const val COOKIE_REQUEST_PARAM_NAME = "Cookie"
private const val COOKIE_RESPONCE_PARAM_NAME = "Set-Cookie"
private const val STRONG_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6InRydWUiLCJpYXQiOjE3MjY4NTIzOTd9.WW2fj_gRrGD2I6BklSHIS03Q8hBUMUhHxX7jDIcKs-s"
private const val NOT_STRONG_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6ImZhbHNlIiwiaWF0IjoxNzI2ODUyMzk3fQ.8Vfa3gaj7nY0Ov5Om5nJFcEs4RbFLaREc_89Fi2wv4U"

class AuthenticationTest {

    @Test
    fun `Do not grand access to a resource protected by strong token auth without having any token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.post("/register")

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grand access to a resource protected by strong token auth with a pre-generated not strong token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.post("/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by strong token auth with the pre-generated strong token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.post("/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grand access to a resource protected by basic token auth without having any token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by basic token auth with a not strong token`() =
        testConfiguredApplication { client, _ ->
            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

            val response0 = client.post("/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(STRONG_TOKEN)
            }
            val basicToken = response0.body<TokenRegistrationResponse>().token

            val response1 = client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(basicToken)
                setBody(inputCredentials)
            }

            assertEquals(
                expected = HttpStatusCode.Created,
                actual = response1.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by basic token auth with a pre-generated strong token`() =
        testConfiguredApplication { client, _ ->
            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

            val response0 = client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(STRONG_TOKEN)
                setBody(inputCredentials)
            }

            assertEquals(
                expected = HttpStatusCode.Created,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grant access to a session-protected resource without having any session`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.get("/v1/users/") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not login with a non-registed username`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        val response0 = client.post("/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register with a blank username`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = " ", password = "qwerty")

        val response0 = client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register with a blank password`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = "admin", password = " ")

        val response0 = client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Register a new user`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        val response0 = client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not login the existing user with the incorrect password`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        // Login the existing user with modified password
        val response0 = client.post("/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials.copy(password = "password"))
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Login the existing user with the correct password`() = testConfiguredApplication { client, _ ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        // Login the existing user
        val response0 = client.post("/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response0.status,
        )
    }

    @Test
    fun `Grant access to the session-protected resource with active session`() =
        testConfiguredApplication { client, _ ->
            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

            // Register the new user
            client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/users/login") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }
            val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

            // Get access to the session-protected resource
            val response1 = client.get("/v1/users/") {
                header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the session-protected resource after logout`() =
        testConfiguredApplication { client, _ ->
            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

            // Register the new user
            client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/users/login") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }
            val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

            // Close the session on the server's side
            client.post("/v1/users/logout") {
                header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            // Try to get access to the session-protected resource after logout
            val response1 = client.get("/v1/users/") {
                header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not generate a new non-strong access token with only the session without a strong token`() =
        testConfiguredApplication { client, _ ->
            val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

            // Register the new user
            client.post("/v1/users/register") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }

            // Login the registed user and extract its cookie
            val response0 = client.post("/v1/users/login") {
                contentType(ContentType.Application.Json)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(inputCredentials)
            }
            val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

            // Try to register a new non-strong access token
            val response1 = client.post("/register") {
                header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response1.status,
            )
        }
}