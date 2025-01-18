package com.yaroslavzghoba

import com.yaroslavzghoba.mappers.toCardRequest
import com.yaroslavzghoba.mappers.toCollectionRequest
import com.yaroslavzghoba.model.*
import com.yaroslavzghoba.security.hashing.HashingServiceImpl
import com.yaroslavzghoba.utils.AuthUtils
import com.yaroslavzghoba.utils.rawCookie
import com.yaroslavzghoba.utils.testConfiguredApplication
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.minutes

private const val SERVER_HOST = "localhost"
private const val SERVER_PORT = 443
private const val NOT_STRONG_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6ImZhbHNlIiwiaWF0IjoxNzI2ODUyMzk3fQ.8Vfa3gaj7nY0Ov5Om5nJFcEs4RbFLaREc_89Fi2wv4U"
private val inputCredentials = InputCredentials(username = "admin", password = "qwerty")
private val anotherInputCredentials = InputCredentials(username = "papagei", password = "password")
private val user = User.Builder(
    inputCredentials = inputCredentials,
    hashingService = HashingServiceImpl(pepper = "pepper", algorithm = "SHA-512"),
).build()
private val collectionRequest = CardCollectionRequest(
    id = null,
    title = "English",
    description = "The collection of useful English words and phrases",
    nativeLanguageISOCode = "ua",
    foreignLanguageISOCode = "en",
)
private val cardRequest = CardRequest(
    id = null,
    nativeLanguageValue = "робити внесок",
    nativeLanguageValueDescription = null,
    nativeLanguageValueExample = "Він робить внесок у суспільство",
    foreignLanguageValue = "to contribute",
    foreignLanguageValueDescription = null,
    foreignLanguageValueExample = "He often contributes to the community",
    correctAnswersInRow = 0,
    nextTimeAt = Clock.System.now(),
)

class RoutingTest {

    @Test
    fun `Receive 500 status code during error on server side`() = testConfiguredApplication { client, _ ->
        val response0 = client.get("/error")
        assertEquals(
            expected = HttpStatusCode.InternalServerError,
            actual = response0.status
        )
    }

    @Test
    fun `Do not insert a new collection if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val body = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        val body1 = response1.body<CardCollection>().toCollectionRequest()
        val response2 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Try to update the collection
        val response1 = client.put("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val body = collectionRequest.copy(id = null)
        val response1 = client.put("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val body = collectionRequest.copy(id = 0)
        val response1 = client.put("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response2.rawCookie()  // Contains the user's session

        // Try to update the collection using the another user's session
        val body1 = response1.body<CardCollection>().toCollectionRequest()
        val response3 = client.put("/v1/collections/") {
            rawCookie(value = rawCookie1)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val insertedCollection = response1.body<CardCollection>()

        // Update the existing collection
        val body1 = body0.copy(id = insertedCollection.id, description = null)
        val response2 = client.put("/v1/collections/") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to delete not existing collection
        val collectionId = "Hello, Papagei!"  // Not valid identifier
        val response1 = client.delete("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to delete not existing collection
        val collectionId = 0
        val response1 = client.delete("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie)
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
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response2.rawCookie()  // Contains the user's session

        // Try to update the collection using the another user's session
        val collectionId = response1.body<CardCollection>().id
        val response3 = client.delete("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie1)
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
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body)
        }
        val insertedCollectionId = response1.body<CardCollection>().id

        // Delete the inserted collection
        val response2 = client.delete("/v1/collections/${insertedCollectionId}") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not insert a new card if collection id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to insert a new card
        val collectionId = "Hello, Papagei!"  // Not valid identifier
        val response1 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(cardRequest)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not insert a new card if there is no parent collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to insert a new card
        val collectionId: Long = 0
        val response1 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(cardRequest)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not insert a card if the collection is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response2.rawCookie()  // Contains the user's session

        // Try to insert a new card
        val body1 = cardRequest
        val response3 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie1)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response3.status,
        )
    }

    @Test
    fun `Do not insert a new card if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to insert a new card
        val body1 = user  // Set a user instead of card
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response2.status,
        )
    }

    @Test
    fun `Insert a new card to the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert a new card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not insert a card if it is already inserted`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert the card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        // Try to insert the same card again
        val body2 = response2.body<Card>().toCardRequest()
        val response3 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body2)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response3.status,
        )
    }

    @Test
    fun `Do not update a card if collection id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to insert a new card
        val collectionId = "Hello, Papagei!"  // Not valid identifier
        val response1 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(cardRequest)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update a card if parent collection not found`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to insert a new card
        val collectionId: Long = 0
        val response1 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(cardRequest)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update a card if the collection is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response2.rawCookie()  // Contains the user's session

        // Try to update the card
        val response3 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie1)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(cardRequest)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response3.status,
        )
    }

    @Test
    fun `Do not update a card if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to update the card
        val body1 = user  // Set a user instead of card
        val response2 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not update a card if it not found in the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert the collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to update not existing card
        val body1 = cardRequest.copy(id = 0)
        val response2 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not update a card if it is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert a new card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }
        val insertedCard = response2.body<Card>()

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response3 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response3.rawCookie()  // Contains the user's session

        // Try to update the card using the another user's session
        val body2 = insertedCard.copy(
            nativeLanguageValueExample = null,
        ).toCardRequest()
        val response4 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie1)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body2)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response4.status,
        )
    }

    @Test
    fun `Update a card in the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert a new card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }
        val insertedCard = response2.body<Card>()

        // Update the card
        val body2 = insertedCard.copy(
            foreignLanguageValueExample = null,
        ).toCardRequest()
        val response3 = client.put("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body2)
        }
        val updatedCard = response3.body<Card>()

        assertNotEquals(
            illegal = insertedCard.foreignLanguageValueExample,
            actual = updatedCard.foreignLanguageValueExample,
        )
    }

    @Test
    fun `Do not delete a card if the collection id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to delete a card
        val collectionId = "Hello, Papagei!"  // Not valid identifier
        val response1 = client.delete("/v1/collections/$collectionId/cards/0") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not delete a card if the collection not found`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Try to delete a card
        val collectionId = 0  // Not valid identifier
        val response1 = client.delete("/v1/collections/$collectionId/cards/0") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not delete a card if the collection is owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Register, login another user and extract its cookie
        AuthUtils.registerUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val response2 = AuthUtils.loginUser(client, anotherInputCredentials, NOT_STRONG_TOKEN)
        val rawCookie1 = response2.rawCookie()  // Contains the user's session

        // Try to delete a card
        val response3 = client.delete("/v1/collections/$collectionId/cards/0") {
            rawCookie(value = rawCookie1)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response3.status,
        )
    }

    @Test
    fun `Do not delete a card if the card id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to delete not existing collection
        val cardId = "Hello, Papagei!"  // Not valid identifier
        val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not delete a card if the card not found`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to delete not existing card
        val cardId = 0
        val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not delete a card if it not found`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Try to delete a card
        val cardId = 0
        val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not delete a card if the passed collection does not correspond the actual one`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
            val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert a new collection
            val body0 = collectionRequest
            val response1 = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(body0)
            }
            val collectionId = response1.body<CardCollection>().id

            // Insert another new collection
            val response2 = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(body0)
            }
            val anotherCollectionId = response2.body<CardCollection>().id

            // Insert a new card
            val body1 = cardRequest
            val response3 = client.post("/v1/collections/$collectionId/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(body1)
            }
            val cardId = response3.body<Card>().id

            // Try to delete a card
            val response4 = client.delete("/v1/collections/$anotherCollectionId/cards/$cardId") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response4.status,
            )
        }

    @Test
    fun `Delete a card from the storage`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert a new card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }
        val cardId = response2.body<Card>().id

        // Delete the inserted collection
        val response3 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response3.status,
        )
    }

    @Test
    fun `Get the card where the next repetition time has arrived`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a new collection
        val body0 = collectionRequest
        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body0)
        }
        val collectionId = response1.body<CardCollection>().id

        // Insert a new card
        val body1 = cardRequest
        val response2 = client.post("/v1/collections/$collectionId/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(NOT_STRONG_TOKEN)
            setBody(body1)
        }
        val insertedCard = response2.body<Card>()

        // Start learning process
        client.webSocket(
            method = HttpMethod.Get,
            host = SERVER_HOST,
            port = SERVER_PORT,
            path = "/v1/collections/$collectionId/cards",
            request = {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
            }
        ) {
            val cards: List<Card> = receiveDeserialized()
            assertEquals(
                expected = insertedCard.nextTimeAt.toEpochMilliseconds(),
                actual = cards[0].nextTimeAt.toEpochMilliseconds(),
            )
        }
    }

    @Test
    fun `Do not get the card where the next repetition time has not arrived`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            AuthUtils.registerUser(client, inputCredentials, NOT_STRONG_TOKEN)
            val response0 = AuthUtils.loginUser(client, inputCredentials, NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert a new collection
            val body0 = collectionRequest
            val response1 = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(body0)
            }
            val collectionId = response1.body<CardCollection>().id

            // Insert a new card
            val body1 = cardRequest.copy(nextTimeAt = Clock.System.now() + 1.minutes)
            client.post("/v1/collections/$collectionId/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(NOT_STRONG_TOKEN)
                setBody(body1)
            }

            // Start learning process
            client.webSocket(
                method = HttpMethod.Get,
                host = SERVER_HOST,
                port = SERVER_PORT,
                path = "/v1/collections/$collectionId/cards",
                request = {
                    rawCookie(value = rawCookie)
                    bearerAuth(NOT_STRONG_TOKEN)
                }
            ) {
                val cards: List<Card> = receiveDeserialized()
                assertEquals(
                    expected = emptyList(),
                    actual = cards,
                )
            }
        }
}

