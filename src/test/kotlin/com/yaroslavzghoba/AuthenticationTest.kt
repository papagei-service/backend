package com.yaroslavzghoba

import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.SignInResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticationTest {

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
    fun `Do not sign up with a blank username`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
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
    fun `Do not sign up with a blank password`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
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
    fun `Sign up a new user`() = testConfiguredApplication { client ->
        val response = client.post("/api/v1/sign-up") {
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
    fun `Do not sign in with an incorrect password`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Sign in the existing user
        val response = client.post("/api/v1/sign-in") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials.copy(password = "password"))
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response.status,
        )
    }

    @Test
    fun `Sign in with a correct password`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Sign in the existing user
        val response = client.post("/api/v1/sign-in") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status,
        )
    }

    @Test
    fun `Grant access to an authorized user`() = testConfiguredApplication { client ->
        val inputCredentials = InputCredentials(username = "admin", password = "qwerty")

        // Sign up the new user
        client.post("/api/v1/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
        }

        // Sign in the existing user & extract the JWT token
        val response0 = client.post("/api/v1/sign-in") {
            contentType(ContentType.Application.Json)
            setBody(inputCredentials)
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