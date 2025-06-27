package com.yaroslavzghoba.routing

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.mappers.toCardUpdateRequest
import com.yaroslavzghoba.mappers.toLoginCredentials
import com.yaroslavzghoba.model.Card
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
private val LOGGER = KtorSimpleLogger(CardsRoutingTest::class.java.name)

class CardsRoutingTest {

    @Test
    fun `001= Do not insert a new card if the request body is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/cards/") {
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
    fun `002= Insert a new card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(TestData.FIRST_CARD_REQUEST)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `003= Do not update the card if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `004= Do not update the card if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val cardUpdateRequest = TestData.FIRST_CARD_REQUEST.toCardUpdateRequest()
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
    fun `005= Do not update the card if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardUpdateRequest = TestData.FIRST_CARD_REQUEST.toCardUpdateRequest()
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
    fun `006= Do not update the card if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardInsertRequest = TestData.FIRST_CARD_REQUEST
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = TestData.SECOND_REGISTRATION_CREDENTIALS
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
    fun `007= Update the card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a card that will be updated and extract its ID
        val cardInsertRequest = TestData.FIRST_CARD_REQUEST
        val insertedCard = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>()

        // Update the card
        val cardUpdateRequest = TestData.SECOND_CARD_REQUEST.toCardUpdateRequest()
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
    fun `008= Do not delete the card if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `009= Do not delete the card if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = TestData.FIRST_REGISTRATION_CREDENTIALS
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
    fun `010= Do not delete the card if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardInsertRequest = TestData.FIRST_CARD_REQUEST
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>().id

        // Register, login another user and extract its cookie
        val registrationCredentials1 = TestData.SECOND_REGISTRATION_CREDENTIALS
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
    fun `011= Delete the card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = TestData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a card that will be deleted and extract its ID
        val cardInsertRequest = TestData.FIRST_CARD_REQUEST
        val insertedCard = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(cardInsertRequest)
        }.body<Card>()

        // Delete the collection
        val response2 = client.delete("/v1/cards/${insertedCard.id}") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }
}