package com.yaroslavzghoba.routing

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.mappers.toCardUpdateRequest
import com.yaroslavzghoba.mappers.toLoginCredentials
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CardsResponse
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
import kotlin.test.assertTrue

@Suppress("unused")
private val LOGGER = KtorSimpleLogger(CardsRoutingTest::class.java.name)

class CardsRoutingTest {

    @Test
    fun `Do not insert a new card if the request body is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)  // Set a registration credentials instead of an example
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Insert a new card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update the card if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.put("/v1/cards/1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)  // Set a registration credentials instead of a card
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update the card if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val cardUpdateRequest = MockData.FIRST_CARD_REQUEST.toCardUpdateRequest()
        val response1 = client.put("/v1/cards/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not update the card if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardUpdateRequest = MockData.FIRST_CARD_REQUEST.toCardUpdateRequest()
            val response1 = client.put("/v1/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardUpdateRequest)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not update the card if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardInsertRequest = MockData.FIRST_CARD_REQUEST
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to update the card created by the first user on behalf of the second user
        val cardUpdateRequest = cardInsertRequest.toCardUpdateRequest()
        val response2 = client.put("/v1/cards/$cardId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `Update the card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a card that will be updated and extract its ID
        val cardInsertRequest = MockData.FIRST_CARD_REQUEST
        val insertedCard = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>()

        // Update the card
        val cardUpdateRequest = MockData.SECOND_CARD_REQUEST.toCardUpdateRequest()
        val response2 = client.put("/v1/cards/${insertedCard.id}") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardUpdateRequest)
        }

        assertEquals(
            expected = cardUpdateRequest
                .toCard(id = insertedCard.id, ownerId = insertedCard.ownerId),
            actual = response2.body<Card>(),
        )
    }

    @Test
    fun `Do not delete the card if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.delete("/v1/cards/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `Do not delete the card if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.delete("/v1/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardInsertRequest = MockData.FIRST_CARD_REQUEST
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to delete the card created by the first user on behalf of the second user
        val response2 = client.delete("/v1/cards/$cardId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `Delete the card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a card that will be deleted and extract its ID
        val cardInsertRequest = MockData.FIRST_CARD_REQUEST
        val insertedCard = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>()

        // Delete the card
        val response2 = client.delete("/v1/cards/${insertedCard.id}") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }

    @Test
    fun `Do not grant access to the card if the id parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to a card using invalid id
            val response1 = client.get("/v1/cards/-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the card if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to a card in empty storage
            val response1 = client.get("/v1/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the card if it owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a card on behalf of the first user
            val cardInsertRequest = MockData.FIRST_CARD_REQUEST
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardInsertRequest)
            }.body<Card>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            // Try to get access to the card created by the first user on behalf of the second user
            val response2 = client.get("/v1/cards/$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Grant access to the card by its id`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a card that will be updated and extract its ID
        val cardInsertRequest = MockData.FIRST_CARD_REQUEST
        val insertedCard = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>()
        val cardId = insertedCard.id!!

        val receivedCard = client.get("/v1/cards/$cardId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<Card>()

        assertEquals(
            expected = insertedCard,
            actual = receivedCard,
        )
    }

    @Test
    fun `Do not grant access to the cards if the limit query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards?limit=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the cards if the offset query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards?offset=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Grant access to the user's cards`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert cards that will be received
        val insertedCards = listOf(
            MockData.FIRST_CARD_REQUEST,
            MockData.SECOND_CARD_REQUEST,
            MockData.THIRD_CARD_REQUEST,
        ).map { cardInsertRequest ->
            client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardInsertRequest)
            }.body<Card>()
        }

        val receivedCards = client.get("/v1/cards") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<CardsResponse>().cards

        assertTrue {
            insertedCards == receivedCards
        }
    }

    @Test
    fun `Grant access to the user's cards with limit query parameter`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert cards that will be received
        val insertedCards = listOf(
            MockData.FIRST_CARD_REQUEST,
            MockData.SECOND_CARD_REQUEST,
            MockData.THIRD_CARD_REQUEST,
        ).map { cardInsertRequest ->
            client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardInsertRequest)
            }.body<Card>()
        }

        val limit = 2
        val response1 = client.get("/v1/cards?limit=$limit") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<CardsResponse>()
        val receivedCards = response1.cards

        assertEquals(expected = insertedCards.size, actual = response1.totalCount.toInt())
        assertEquals(
            expected = insertedCards.subList(fromIndex = 0, toIndex = limit),
            actual = receivedCards,
        )
    }

    @Test
    fun `Grant access to the user's cards with offset query parameter`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert cards that will be received
        val insertedCards = listOf(
            MockData.FIRST_CARD_REQUEST,
            MockData.SECOND_CARD_REQUEST,
            MockData.THIRD_CARD_REQUEST,
        ).map { cardInsertRequest ->
            client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardInsertRequest)
            }.body<Card>()
        }

        val limit = 2
        val offset = 1
        val response1 = client.get("/v1/cards?limit=$limit&offset=$offset") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<CardsResponse>()
        val receivedCards = response1.cards

        assertEquals(expected = insertedCards.size, actual = response1.totalCount.toInt())
        assertEquals(
            expected = insertedCards.subList(fromIndex = 0 + offset, toIndex = limit + offset),
            actual = receivedCards,
        )
    }

    @Test
    fun `Do not grant access to the cards if the collection_id query parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards?collection_id=-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the cards if the collection with id equals to collection_id was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards?collection_id=1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the cards if the collection owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            val response2 = client.get("/v1/cards?collection_id=$collectionId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Grant access to cards that belong the passed collection`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // The collection to which the cards will belong
            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            // Insert cards and add them to the collection
            val expectedCards = listOf(
                MockData.FIRST_CARD_REQUEST,
                MockData.SECOND_CARD_REQUEST,
                MockData.THIRD_CARD_REQUEST,
            ).map { cardInsertRequest ->
                val card = client.post("/v1/cards/") {
                    rawCookie(value = rawCookie0)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(cardInsertRequest)
                }.body<Card>()
                val cardId = card.id
                client.post("/v1/collections/$collectionId/cards/$cardId") {
                    rawCookie(value = rawCookie0)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                }
                card
            }

            // Get cards which belong to the collection
            val actualCards = client.get("/v1/cards?collection_id=$collectionId") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CardsResponse>().cards

            assertEquals(
                expected = expectedCards,
                actual = actualCards,
            )
        }

    @Test
    fun `Grant access to the cards by collection id with limit query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // The collection to which the cards will belong
            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            // Insert cards that will be received
            val insertedCards = listOf(
                MockData.FIRST_CARD_REQUEST,
                MockData.SECOND_CARD_REQUEST,
                MockData.THIRD_CARD_REQUEST,
            ).map { cardInsertRequest ->
                val card = client.post("/v1/cards/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(cardInsertRequest)
                }.body<Card>()
                val cardId = card.id
                client.post("/v1/collections/$collectionId/cards/$cardId") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                }
                card
            }

            val limit = 2
            val response1 = client.get("/v1/cards?limit=$limit") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CardsResponse>()
            val receivedCards = response1.cards

            assertEquals(expected = insertedCards.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCards.subList(fromIndex = 0, toIndex = limit),
                receivedCards,
            )
        }

    @Test
    fun `Grant access to the cards by collection id with offset query parameter`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Insert cards that will be received
            val insertedCards = listOf(
                MockData.FIRST_CARD_REQUEST,
                MockData.SECOND_CARD_REQUEST,
                MockData.THIRD_CARD_REQUEST,
            ).map { cardInsertRequest ->
                client.post("/v1/cards/") {
                    rawCookie(value = rawCookie)
                    bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                    setBody(cardInsertRequest)
                }.body<Card>()
            }

            val limit = 2
            val offset = 1
            val response1 = client.get("/v1/cards?limit=$limit&offset=$offset") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }.body<CardsResponse>()
            val receivedCards = response1.cards

            assertEquals(expected = insertedCards.size, actual = response1.totalCount.toInt())
            assertEquals(
                expected = insertedCards.subList(fromIndex = 0 + offset, toIndex = limit + offset),
                actual = receivedCards,
            )
        }

    @Test
    fun `Do not add the card to the collection if the collection_id path parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/collections/-1/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not add the card to the collection if the card_id path parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/collections/1/cards/-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not add the card to the collection if the corresponding collection was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val response1 = client.post("/v1/collections/1/cards/$cardId") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not add the card to the collection if the corresponding card was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            val response1 = client.post("/v1/collections/$collectionId/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not add the card to the collection if the corresponding card owned by another user`() =
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

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            val response2 = client.post("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Do not add the card to the collection if the corresponding collection owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val response2 = client.post("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Add the card to the collection`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // A collection to which the card will belong
            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id
            // The card that will belong to the collections
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Add the card to the collection
            val response1 = client.post("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Created,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the collection_id path parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.delete("/v1/collections/-1/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the card_id path parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.delete("/v1/collections/1/cards/-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the corresponding collection was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val response1 = client.delete("/v1/collections/1/cards/$cardId") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the corresponding card was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            val response1 = client.delete("/v1/collections/$collectionId/cards/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the corresponding card owned by another user`() =
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

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Do not delete the card from the collection if the corresponding collection owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `Delete the card from the collection`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val collectionId = client.post("/v1/collections/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_COLLECTION_INSERT_REQUEST)
            }.body<CardCollection>().id

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val response2 = client.delete("/v1/collections/$collectionId/cards/$cardId") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NoContent,
                actual = response2.status,
            )
        }
}