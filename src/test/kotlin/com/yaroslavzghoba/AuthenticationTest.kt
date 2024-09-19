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

class AuthenticationTest {

    @Test
    fun `Do not grant access to a token-protected resource without token`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/v1/users/register")
        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Generate an API access token`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register")
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response0.status,
        )
    }

    @Test
    fun `Grand access to a token-protected resource with a token`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
        val response1 = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not grant access to a session-protected resource without session`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val response1 = client.post("/api/v1/users/logout") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not login with a non-registed username`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
        val response1 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not register with a blank username`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val inputCredentials = InputCredentials(username = " ", password = "qwerty")
        val response1 = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not register with a blank password`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val inputCredentials = InputCredentials(username = "admin", password = " ")
        val response1 = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response1.status,
        )
    }

    @Test
    fun `Register a new user`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
        val response = client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Do not login with an incorrect password`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        // Login the existing user with modified password
        val response1 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials.copy(password = "password"))
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response1.status,
        )
    }

    @Test
    fun `Login with a correct password`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token

        // Register the new user
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        // Login the existing user
        val response1 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response1.status,
        )
    }

    @Test
    fun `Grant access to the session-protected resource with session`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }

        // Login the user and extract its cookie
        val response1 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }
        val cookies = response1.headers[COOKIE_RESPONCE_PARAM_NAME]

        // Get access to the session-protected resource
        val response2 = client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(token)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not grant access to the session-protected resource after logout`() = testConfiguredApplication { client ->
        val response0 = client.post("/api/register") {
            contentType(ContentType.Application.Json)
        }
        val token = response0.body<TokenRegistrationResponse>().token
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Register the new user
        client.post("/api/v1/users/register") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Login the user and extract its cookie
        val response1 = client.post("/api/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }
        val cookies = response1.headers[COOKIE_RESPONCE_PARAM_NAME]

        // Close the session on the server's side
        client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(token)
        }

        // Get access to the session-protected resource after logout
        val response2 = client.post("/api/v1/users/logout") {
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(token)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response2.status,
        )
    }
}