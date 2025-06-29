package com.yaroslavzghoba.routing

import com.yaroslavzghoba.mappers.toExample
import com.yaroslavzghoba.mappers.toExampleUpdateRequest
import com.yaroslavzghoba.mappers.toLoginCredentials
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.Example
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
private val LOGGER = KtorSimpleLogger(ExamplesRoutingTest::class.java.name)

class ExamplesRoutingTest {

    @Test
    fun `001= Do not insert a new example if the request body is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/cards/1/examples") {
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
    fun `002= Do not insert a new example if the card id parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/cards/-1/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `003= Do not insert a new example if the parent card is not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.post("/v1/cards/1/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `004= Do not insert a new example if the parent card owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a card on behalf of the first user
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id!!

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            // Create an example using the first user's card behalf of the second user
            val response2 = client.post("/v1/cards/$cardId/examples") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `005= Insert a new example`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id

        val response1 = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response1.status,
        )
    }

    @Test
    fun `006= Do not update the example if the request body is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.put("/v1/examples/1") {
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
    fun `007= Do not update the example if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id

        val exampleUpdateRequest = MockData.FIRST_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId!!)
        val response1 = client.put("/v1/examples/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(exampleUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `008= Do not update the example if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val exampleUpdateRequest = MockData.FIRST_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId!!)
            val response1 = client.put("/v1/examples/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(exampleUpdateRequest)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `009= Do not update the example if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id!!
        // Create an example on behalf of the first user
        val exampleId = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>().id!!

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to update the example created by the first user on behalf of the second user
        val exampleUpdateRequest = MockData.SECOND_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId)
        val response2 = client.put("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(exampleUpdateRequest)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `010= Do not update the example if its new parent card was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            val exampleId = client.post("/v1/cards/$cardId/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }.body<Example>().id

            val wrongCardId = cardId!! + 1  // Non-existing card id
            val exampleUpdateRequest = MockData.FIRST_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = wrongCardId)
            val response1 = client.put("/v1/examples/$exampleId") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(exampleUpdateRequest)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `011= Do not update the example if its new parent card owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a card on behalf of the first user
            val cardId0 = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id!!

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            // Create a card on behalf of the second user
            val cardId1 = client.post("/v1/cards/") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id!!

            // Create an example on behalf of the second user
            val exampleId = client.post("/v1/cards/$cardId1/examples") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }.body<Example>().id!!

            // Try to update the second user's example using the card that belongs to the first user
            val exampleUpdateRequest = MockData.SECOND_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId0)
            val response2 = client.put("/v1/examples/$exampleId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(exampleUpdateRequest)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `012= Update the example`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a parent card
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id!!

        // Insert an example that will be updated
        val insertedExample = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>()
        val exampleId = insertedExample.id!!

        // Update the example
        val exampleUpdateRequest = MockData.SECOND_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId)
        val updatedExample = client.put("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(exampleUpdateRequest)
        }.body<Example>()

        assertEquals(
            expected = exampleUpdateRequest.toExample(id = exampleId),
            actual = updatedExample,
        )
    }

    @Test
    fun `013= Update the example including its parent card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a first parent card
        val cardId0 = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id!!

        // Insert a second parent card
        val cardId1 = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id!!

        // Insert an example that will be updated and relate it to the first card
        val insertedExample = client.post("/v1/cards/$cardId0/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>()
        val exampleId = insertedExample.id!!

        // Update the example including its parent card id
        val exampleUpdateRequest = MockData.SECOND_EXAMPLE_REQUEST.toExampleUpdateRequest(cardId = cardId1)
        val updatedExample = client.put("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(exampleUpdateRequest)
        }.body<Example>()

        assertEquals(
            expected = exampleUpdateRequest.toExample(id = exampleId),
            actual = updatedExample,
        )
    }

    @Test
    fun `014= Do not delete the example if the id parameter is invalid`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        val response1 = client.delete("/v1/examples/-1") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response1.status,
        )
    }

    @Test
    fun `015= Do not delete the example if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.delete("/v1/examples/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `016= Do not delete the example if it owned by another user`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Create a card on behalf of the first user
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id

        // Create an example on behalf of the first user
        val exampleId = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie0)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>().id!!

        // Register, login another user and extract its cookie
        val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
        val response1 = AuthUtils
            .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        // Try to delete the example created by the first user on behalf of the second user
        val response2 = client.delete("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie1)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.Forbidden,
            actual = response2.status,
        )
    }

    @Test
    fun `017= Delete the example`() = testConfiguredApplication { client, _ ->
        // Register, login a first user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Create a parent card
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id

        // Create an example that will be deleted
        val exampleId = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>().id!!

        // Delete the example from the storage
        val response2 = client.delete("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }

        assertEquals(
            expected = HttpStatusCode.NoContent,
            actual = response2.status,
        )
    }

    @Test
    fun `018= Do not grant access to the example if the id parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to an example using invalid id
            val response1 = client.get("/v1/examples/-1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `019= Do not grant access to the example if it was not found in the storage`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Try to get access to an example in empty storage
            val response1 = client.get("/v1/examples/1") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `020= Do not grant access to the example if it owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a parent card on behalf of the first user
            val cardId = client.post("/v1/cards/") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_CARD_REQUEST)
            }.body<Card>().id

            // Create an example on behalf of the first user
            val exampleId = client.post("/v1/cards/$cardId/examples") {
                rawCookie(value = rawCookie0)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(MockData.FIRST_EXAMPLE_REQUEST)
            }.body<Example>().id

            // Register, login another user and extract its cookie
            val registrationCredentials1 = MockData.SECOND_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials1, AuthUtils.NOT_STRONG_TOKEN)
            val response1 = AuthUtils
                .loginUser(client, registrationCredentials1.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie1 = response1.rawCookie()  // Contains the user's session

            // Try to get access to the example created by the first user
            val response2 = client.get("/v1/examples/$exampleId") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `021= Grant access to the example by its id`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Create a parent card
        val cardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id

        // Insert an example that will be received
        val insertedExample = client.post("/v1/cards/$cardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_EXAMPLE_REQUEST)
        }.body<Example>()
        val exampleId = insertedExample.id!!

        // Receive the example by its id
        val receivedExample = client.get("/v1/examples/$exampleId") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<Example>()

        assertEquals(
            expected = insertedExample,
            actual = receivedExample,
        )
    }

    @Test
    fun `022= Do not grant access to the examples if id parameter is invalid`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards/-1/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response1.status,
            )
        }

    @Test
    fun `023= Do not grant access to the examples if parent card was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards/1/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `024= Do not grant access to the examples if parent card was not found`() =
        testConfiguredApplication { client, _ ->
            // Register, login a user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie = response0.rawCookie()  // Contains the user's session

            val response1 = client.get("/v1/cards/1/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response1.status,
            )
        }

    @Test
    fun `025= Do not grant access to the examples if parent card owned by another user`() =
        testConfiguredApplication { client, _ ->
            // Register, login a first user and extract its cookie
            val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
            AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
            val response0 = AuthUtils
                .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Create a parent card on behalf of the first user
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

            // Try to get access to the examples created by the first user
            val response2 = client.get("/v1/cards/$cardId/examples") {
                rawCookie(value = rawCookie1)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Forbidden,
                actual = response2.status,
            )
        }

    @Test
    fun `026= Grant access to the examples by card`() = testConfiguredApplication { client, _ ->
        // Register, login a user and extract its cookie
        val registrationCredentials0 = MockData.FIRST_REGISTRATION_CREDENTIALS
        AuthUtils.registerUser(client, registrationCredentials0, AuthUtils.NOT_STRONG_TOKEN)
        val response0 = AuthUtils
            .loginUser(client, registrationCredentials0.toLoginCredentials(), AuthUtils.NOT_STRONG_TOKEN)
        val rawCookie = response0.rawCookie()  // Contains the user's session

        // Insert a first parent card
        val firstCardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.FIRST_CARD_REQUEST)
        }.body<Card>().id!!

        // Insert a second parent card
        val secondCardId = client.post("/v1/cards/") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.SECOND_CARD_REQUEST)
        }.body<Card>().id!!

        // Insert examples into first card
        val insertedIntoFirstCard = listOf(
            MockData.FIRST_EXAMPLE_REQUEST,
            MockData.SECOND_EXAMPLE_REQUEST,
        ).map { cardInsertRequest ->
            client.post("/v1/cards/$firstCardId/examples") {
                rawCookie(value = rawCookie)
                bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
                setBody(cardInsertRequest)
            }.body<Example>()
        }

        // Insert an example into second card as well
        client.post("/v1/cards/$secondCardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
            setBody(MockData.THIRD_EXAMPLE_REQUEST)
        }

        val receivedFromFirstCard = client.get("/v1/cards/$firstCardId/examples") {
            rawCookie(value = rawCookie)
            bearerAuth(AuthUtils.NOT_STRONG_TOKEN)
        }.body<List<Example>>()

        assertTrue {
            insertedIntoFirstCard == receivedFromFirstCard
        }
    }
}