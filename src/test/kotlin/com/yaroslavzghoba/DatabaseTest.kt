package com.yaroslavzghoba

import com.yaroslavzghoba.mappers.toCollectionRequest
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CollectionRequest
import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.utils.AuthUtils
import com.yaroslavzghoba.utils.SubjectType
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val COOKIE_REQUEST_PARAM_NAME = "Cookie"
private const val COOKIE_RESPONCE_PARAM_NAME = "Set-Cookie"
private const val NOT_STRONG_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6ImZhbHNlIiwiaWF0IjoxNzI2ODUyMzk3fQ.8Vfa3gaj7nY0Ov5Om5nJFcEs4RbFLaREc_89Fi2wv4U"
private val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
private val anotherInputCredentials = InputCredentials(username = "papagei", password = "password")
private val user = User.Builder(
    inputCredentials = inputCredentials,
    hashingService = HashingServiceImpl(pepper = "pepper", algorithm = "SHA-512"),
).build()
private val collectionRequest = CollectionRequest(
    id = null,
    title = "English",
    description = "The collection of useful English words and phrases",
    subjectType = SubjectType.FOREIGN_LANGUAGE,
    subjectLanguage = "en",
    nativeLanguage = "ua",
)

class DatabaseTest {

    @Test
    fun `Do not insert a new collection if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(user)  // Set a user instead of a collection
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Insert a new collection to the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        val body = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not insert a collection if it is already inserted`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        val body1 = response1.body<CardCollection>().toCollectionRequest()
        val response2 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not update a collection if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Try to update the collection
        val response1 = client.put("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(user)  // Set a user instead of the existing collection
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update a collection with a null identifier`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        val body = collectionRequest.copy(id = null)
        val response1 = client.put("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update a collection if it is not in the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        val body = collectionRequest.copy(id = 0)
        val response1 = client.put("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update a collection if it is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies0 = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val cookies1 = response2.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Try to update the collection using the another user's session
        val body1 = response1.body<CardCollection>().toCollectionRequest()
        val response3 = client.put("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies1)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response3.status,
        )
    }

    @Test
    fun `Update a collection in the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val insertedCollection = response1.body<CardCollection>()

        // Update the existing collection
        val body1 = body0.copy(id = insertedCollection.id, description = null)
        val response2 = client.put("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }
        val updatedCollection = response2.body<CardCollection>()

        assertNotEquals(
            illegal = insertedCollection.description,
            actual = updatedCollection.description,
        )
    }

    @Test
    fun `Do not delete a collection if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Try to delete not existing collection
        val collectionId = "Hello, Papagei!"  // Not valid identifier
        val response1 = client.delete("/v1/collections/$collectionId") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not delete a collection if it does not exist`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Try to delete not existing collection
        val collectionId = 0
        val response1 = client.delete("/v1/collections/$collectionId") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not delete a collection if it is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies0 = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val cookies1 = response2.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Try to update the collection using the another user's session
        val collectionId = response1.body<CardCollection>().id
        val response3 = client.delete("/v1/collections/$collectionId") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies1)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response3.status,
        )
    }

    @Test
    fun `Delete a collection from the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val cookies = response0.headers[COOKIE_RESPONCE_PARAM_NAME]  // Contains the user's session

        // Insert a new collection
        val body = collectionRequest
        val response1 = client.post("/v1/collections/") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body)
        }
        val insertedCollectionId = response1.body<CardCollection>().id

        // Delete the inserted collection
        val response2 = client.delete("/v1/collections/${insertedCollectionId}") {
            contentType(ContentType.Application.Json)
            header(key = COOKIE_REQUEST_PARAM_NAME, value = cookies)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }
}

