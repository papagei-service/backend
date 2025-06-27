package com.yaroslavzghoba.routing

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.mappers.toCardCollectionUpdateRequest
import com.yaroslavzghoba.mappers.toLoginCredentials
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.utils.AuthUtils
import com.yaroslavzghoba.utils.TestData
import com.yaroslavzghoba.utils.rawCookie
import com.yaroslavzghoba.utils.testConfiguredApplication
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("unused")
private val LOGGER = KtorSimpleLogger(CollectionsRoutingTest::class.java.name)

class CollectionsRoutingTest {

    @Test
    fun `001= Do not insert a new collection if the request body is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)  // Set a registration credentials instead of a collection
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `002= Insert a new collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(TestData.FIRST_COLLECTION_INSERT_REQUEST)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `003= Do not update the collection if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 =  client.put("/v1/collections/1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)  // Set a registration credentials instead of a collection
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `004= Do not update the collection if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val collectionUpdateRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
        val response1 = client.put("/v1/collections/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `005= Do not update the collection if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val collectionUpdateRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
            val response1 = client.put("/v1/collections/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(collectionUpdateRequest)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `006= Do not update the collection if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a collection on behalf of the first user
        val collectionInsertRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST
        val collectionId = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = TestData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to update the collection created by the first user on behalf of the second user
        val collectionUpdateRequest = collectionInsertRequest.toCardCollectionUpdateRequest()
        val response2 = client.put("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `007= Update the collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a collection that will be updated and extract its ID
        val collectionInsertRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST
        val insertedCollection = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>()

        // Update the collection
        val collectionUpdateRequest = TestData.SECOND_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
        val response2 = client.put("/v1/collections/${insertedCollection.id}") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionUpdateRequest)
        }

        assertEquals(
            expected = collectionUpdateRequest
                .toCardCollection(id = insertedCollection.id, ownerId = insertedCollection.ownerId),
            actual = response2.body<CardCollection>(),
        )
    }

    @Test
    fun `008= Do not delete the collection if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.delete("/v1/collections/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `009= Do not delete the collection if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.delete("/v1/collections/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `010= Do not delete the collection if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a collection on behalf of the first user
        val collectionInsertRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST
        val collectionId = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = TestData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to delete the collection created by the first user on behalf of the second user
        val response2 = client.delete("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `011= Delete the collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a collection that will be updated and extract its ID
        val collectionInsertRequest = TestData.FIRST_COLLECTION_INSERT_REQUEST
        val insertedCollection = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>()

        // Update the collection
        val response2 = client.delete("/v1/collections/${insertedCollection.id}") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }
}