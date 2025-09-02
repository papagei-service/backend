package com.yaroslavzghoba.routing

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.mappers.toCardCollectionUpdateRequest
import com.yaroslavzghoba.mappers.toLoginCredentials
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CollectionsResponse
import com.yaroslavzghoba.utils.AuthUtils
import com.yaroslavzghoba.utils.MockData
import com.yaroslavzghoba.utils.rawCookie
import com.yaroslavzghoba.utils.testConfiguredApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("unused")
private val LOGGER = KtorSimpleLogger(CollectionsRoutingTest::class.java.name)

class CollectionsRoutingTest {

    @Test
    fun `Do not insert a new collection if the request body is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `Insert a new collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update the collection if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.put("/v1/collections/1") {
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
    fun `Do not update the collection if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val collectionUpdateRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
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
    fun `Do not update the collection if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val collectionUpdateRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
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
    fun `Do not update the collection if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a collection on behalf of the first user
        val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
        val collectionId = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
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
    fun `Update the collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a collection that will be updated and extract its ID
        val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
        val insertedCollection = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>()

        // Update the collection
        val collectionUpdateRequest = MockData.SECOND_COLLECTION_INSERT_REQUEST.toCardCollectionUpdateRequest()
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
    fun `Do not delete the collection if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `Do not delete the collection if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `Do not delete the collection if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a collection on behalf of the first user
        val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
        val collectionId = client.post("/v1/collections/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
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
    fun `Delete the collection`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a collection that will be updated and extract its ID
        val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
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

    @Test
    fun `Do not grant access to the collection if the id parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to a collection using invalid id
            val response1 = client.get("/v1/collections/-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the collection if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to a collection in empty storage
            val response1 = client.get("/v1/collections/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the collection if it owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a collection on behalf of the first user
            val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(collectionInsertRequest)
            }.body<CardCollection>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            // Try to get access to the collection created by the first user on behalf of the second user
            val response2 = client.get("/v1/collections/$collectionId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Grant access to the collection by its id`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a collection that will be updated and extract its ID
        val collectionInsertRequest = MockData.FIRST_COLLECTION_INSERT_REQUEST
        val insertedCollection = client.post("/v1/collections/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(collectionInsertRequest)
        }.body<CardCollection>()
        val collectionId = insertedCollection.id!!

        val receivedCollection = client.get("/v1/collections/$collectionId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<CardCollection>()

        assertEquals(
            expected = insertedCollection,
            actual = receivedCollection,
        )
    }

    @Test
    fun `Do not grant access to the collections if the limit query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/collections?limit=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the collections if the offset query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/collections?offset=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Grant access to the user's collections`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert collections that will be received
        val insertedCollections = listOf(
            MockData.FIRST_COLLECTION_INSERT_REQUEST,
            MockData.SECOND_COLLECTION_INSERT_REQUEST,
            MockData.THIRD_COLLECTION_INSERT_REQUEST,
        ).map { collectionInsertRequest ->
            client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(collectionInsertRequest)
            }.body<CardCollection>()
        }

        val response1 = client.get("/v1/collections") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<CollectionsResponse>()
        val receivedCollections = response1.collections

        assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
        assertEquals(
            expected = insertedCollections,
            actual = receivedCollections,
        )
    }

    @Test
    fun `Grant access to the user's collections with limit query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert cards that will be received
            val insertedCollections = listOf(
                MockData.FIRST_COLLECTION_INSERT_REQUEST,
                MockData.SECOND_COLLECTION_INSERT_REQUEST,
                MockData.THIRD_COLLECTION_INSERT_REQUEST,
            ).map { collectionInsertRequest ->
                client.post("/v1/collections/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(collectionInsertRequest)
                }.body<CardCollection>()
            }

            val limit = 2
            val response1 = client.get("/v1/collections?limit=$limit") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CollectionsResponse>()
            val receivedCollections = response1.collections

            assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCollections.subList(fromIndex = 0, toIndex = limit),
                actual = receivedCollections,
            )
        }

    @Test
    fun `Grant access to the user's collections with offset query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert cards that will be received
            val insertedCollections = listOf(
                MockData.FIRST_COLLECTION_INSERT_REQUEST,
                MockData.SECOND_COLLECTION_INSERT_REQUEST,
                MockData.THIRD_COLLECTION_INSERT_REQUEST,
            ).map { collectionInsertRequest ->
                client.post("/v1/collections/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(collectionInsertRequest)
                }.body<CardCollection>()
            }

            val limit = 2
            val offset = 1
            val response1 = client.get("/v1/collections?limit=$limit&offset=$offset") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CollectionsResponse>()
            val receivedCollections = response1.collections

            assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCollections.subList(fromIndex = 0 + offset, toIndex = limit + offset),
                actual = receivedCollections,
            )
        }

    @Test
    fun `Do not grant access to the collections if the card_id query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/collections?card_id=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the collections if the card with id equals to card_id was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/collections?card_id=1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the collections if the card owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            val response2 = client.get("/v1/collections?card_id=$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Grant access to collections to which the passed card belongs`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // A card that will belong to collections
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Insert collections and add the card to them
            val insertedCollections = listOf(
                MockData.FIRST_COLLECTION_INSERT_REQUEST,
                MockData.SECOND_COLLECTION_INSERT_REQUEST,
                MockData.THIRD_COLLECTION_INSERT_REQUEST,
            ).map { collectionInsertRequest ->
                val collection = client.post("/v1/collections/") {
                    rawCookie(value = rawCookie0)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(collectionInsertRequest)
                }.body<CardCollection>()
                val collectionId = collection.id
                client.post("/v1/collections/$collectionId/cards/$cardId") {
                    rawCookie(value = rawCookie0)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                }
                collection
            }

            // Get collections that contain the card
            val response1 = client.get("/v1/collections?card_id=$cardId") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CollectionsResponse>()
            val receivedCollections = response1.collections

            assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCollections,
                actual = receivedCollections,
            )
        }

    @Test
    fun `Grant access to the collections by card id with limit query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert a card that will belong to the collections
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Insert collections to which the card will belong and add the card to them
            val insertedCollections = listOf(
                MockData.FIRST_COLLECTION_INSERT_REQUEST,
                MockData.SECOND_COLLECTION_INSERT_REQUEST,
                MockData.THIRD_COLLECTION_INSERT_REQUEST,
            ).map { collectionInsertRequest ->
                val collection = client.post("/v1/collections/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(collectionInsertRequest)
                }.body<CardCollection>()
                val collectionId = collection.id
                client.post("/v1/collections/$collectionId/cards/$cardId") {
                    rawCookie(rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                }
                collection
            }

            val limit = 2
            val response1 = client.get("/v1/collections?limit=$limit") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CollectionsResponse>()
            val receivedCollections = response1.collections

            assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCollections.subList(fromIndex = 0, toIndex = limit),
                actual = receivedCollections,
            )
        }

    @Test
    fun `Grant access to the collections by card id with offset query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert a card that will belong to the collections
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Insert collections to which the card will belong and add the card to them
            val insertedCollections = listOf(
                MockData.FIRST_COLLECTION_INSERT_REQUEST,
                MockData.SECOND_COLLECTION_INSERT_REQUEST,
                MockData.THIRD_COLLECTION_INSERT_REQUEST,
            ).map { collectionInsertRequest ->
                val collection = client.post("/v1/collections/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(collectionInsertRequest)
                }.body<CardCollection>()
                val collectionId = collection.id
                client.post("/v1/collections/$collectionId/cards/$cardId") {
                    rawCookie(rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                }
                collection
            }

            val limit = 2
            val offset = 1
            val response1 = client.get("/v1/collections?limit=$limit&offset=$offset") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CollectionsResponse>()
            val receivedCollections = response1.collections

            assertEquals(expected = insertedCollections.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCollections.subList(fromIndex = 0 + offset, toIndex = limit + offset),
                actual = receivedCollections,
            )
        }
}