package space.zghoba.routing

import space.zghoba.mappers.toLoginCredentials
import space.zghoba.model.Account
import space.zghoba.model.TokenResponse
import space.zghoba.utils.AuthUtils
import space.zghoba.utils.MockData
import space.zghoba.utils.rawCookie
import space.zghoba.utils.testConfiguredApplication
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("unused")
private val LOGGER = KtorSimpleLogger(AuthenticationTest::class.java.name)

class AuthenticationTest {

    @Test
    fun `Do not grand access to a resource protected by strong token auth without having any token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.get("/v1/token")

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grand access to a resource protected by strong token auth with a not strong token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.get("/v1/token") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by strong token auth with a strong token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.get("/v1/token") {
                bearerAuth(token = AuthUtils.STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grand access to a resource protected by not strong token auth without having any token`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.post("/v1/account/register")

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by not strong token auth with a not strong token`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS

            val response0 = client.get("/v1/token") {
                bearerAuth(token = AuthUtils.STRONG_TOKEN)
            }
            val notStrongToken = response0.body<TokenResponse>().token

            val response1 = client.post("/v1/account/register") {
                bearerAuth(notStrongToken)
                setBody(registrationCredentials)
            }

            assertEquals(
                expected = HttpStatusCode.Created,
                actual = response1.status,
            )
        }

    @Test
    fun `Grand access to a resource protected by not strong token auth with a strong token`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS

            val response0 = client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            assertEquals(
                expected = HttpStatusCode.Created,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not grant access to a session-protected resource without having any session`() =
        testConfiguredApplication { client, _ ->
            val response0 = client.get("/v1/account") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response0.status,
            )
        }

    @Test
    fun `Do not login with a non-existing username`() = testConfiguredApplication { client, _ ->
        val loginCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS.toLoginCredentials()

        val response0 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register if the request body is invalid`() = testConfiguredApplication { client, _ ->
        val loginCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS.toLoginCredentials()

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)  // Set a login credentials instead of registration credentials
        }

        assertEquals(
            expected = HttpStatusCode.BadRequest,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register with a blank username`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            .copy(username = " ")

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register with a blank display name`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            .copy(displayName = " ")

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register with a blank password`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            .copy(password = " ")

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not register if a username is already taken`() = testConfiguredApplication { client, _ ->
        val firstRegistrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val secondRegistrationCredentials = MockData.SECOND_REGISTRATION_CREDENTIALS
            .copy(username = firstRegistrationCredentials.username)

        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(firstRegistrationCredentials)
        }

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(secondRegistrationCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Register a new user`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS

        val response0 = client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Created,
            actual = response0.status,
        )
    }

    @Test
    fun `Do not login the existing user with the incorrect password`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val wrongLoginCredentials = registrationCredentials.toLoginCredentials()
            .copy(password = MockData.FIRST_REGISTRATION_CREDENTIALS.password + ".")  // Modified password

        // Register the new user
        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        // Login the existing user with modified password
        val response0 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(wrongLoginCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.Unauthorized,
            actual = response0.status,
        )
    }

    @Test
    fun `Login the existing user with the correct password`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val loginCredentials = registrationCredentials.toLoginCredentials()

        // Register the new user
        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        // Login the existing user
        val response0 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response0.status,
        )
    }

    @Test
    fun `Log in the existing user a second time`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val loginCredentials = registrationCredentials.toLoginCredentials()

        // Register the new user
        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        // Login the existing user a first time
        client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }

        // Login the existing user a first time
        val response0 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }

        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response0.status,
        )
    }

    @Test
    fun `Provide a new session during second-time login`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val loginCredentials = registrationCredentials.toLoginCredentials()

        // Register the new user
        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        // Login the existing user a first time
        val response0 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }
        val rawCookie0 = response0.rawCookie()  // Contains the user's session

        // Login the existing user a second time
        val response1 = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            rawCookie(rawCookie0)
            setBody(loginCredentials)
        }
        val rawCookie1 = response1.rawCookie()  // Contains the user's session

        assertEquals(expected = HttpStatusCode.OK, actual = response1.status)
        assertTrue { rawCookie0 != rawCookie1 }  // Session id has been changed
    }

    @Test
    fun `Grant access to the session-protected resource with the previous session id`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            val loginCredentials = registrationCredentials.toLoginCredentials()

            // Register the new user
            client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            // Login the existing user a first time
            val response0 = client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(loginCredentials)
            }
            val rawCookie0 = response0.rawCookie()  // Contains the user's session

            // Login the existing user a second time
            client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                rawCookie(rawCookie0)
                setBody(loginCredentials)
            }

            // Get access to the session-protected resource
            val response1 = client.get("/v1/account") {
                rawCookie(value = rawCookie0)
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(expected = HttpStatusCode.OK, actual = response1.status)
        }

    @Test
    fun `Grant access to the session-protected resource with active session`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            val loginCredentials = registrationCredentials.toLoginCredentials()

            // Register the new user
            client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(loginCredentials)
            }
            val rawCookie = response0.rawCookie()  // Contains the user's session id

            // Get access to the session-protected resource
            val response1 = client.get("/v1/account") {
                rawCookie(value = rawCookie)
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the session-protected resource after logout`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            val loginCredentials = registrationCredentials.toLoginCredentials()

            // Register the new user
            client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(loginCredentials)
            }
            val rawCookie = response0.rawCookie()  // Contains the user's session

            // Close the session on the server's side
            client.post("/v1/account/logout") {
                rawCookie(value = rawCookie)
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            // Try to get access to the session-protected resource after logout
            val response1 = client.get("/v1/account") {
                rawCookie(value = rawCookie)
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not generate a new not strong token with only the session without a strong token`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            val loginCredentials = registrationCredentials.toLoginCredentials()

            // Register the new user
            client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(loginCredentials)
            }
            val cookies = response0.rawCookie()  // Contains the user's session

            // Try to register a new non-strong access token
            val response1 = client.get("/v1/token") {
                rawCookie(value = cookies)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response1.status,
            )
        }

    @Test
    fun `Do not grant access to the session-protected resource after deleting the account`() =
        testConfiguredApplication { client, _ ->
            val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
            val loginCredentials = registrationCredentials.toLoginCredentials()

            // Register the new user
            client.post("/v1/account/register") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(registrationCredentials)
            }

            // Login the user and extract its cookie
            val response0 = client.post("/v1/account/login") {
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
                setBody(loginCredentials)
            }
            val rawCookie = response0.rawCookie()  // Contains the user's session

            client.delete("/v1/account") {
                rawCookie(value = rawCookie)
                bearerAuth(token = AuthUtils.STRONG_TOKEN)
            }

            // Try to get access to the session-protected resource after deleting the account
            val response1 = client.get("/v1/account") {
                rawCookie(value = rawCookie)
                bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            }

            assertEquals(
                expected = HttpStatusCode.Unauthorized,
                actual = response1.status,
            )
        }

    @Test
    fun `Grant access to the account details`() = testConfiguredApplication { client, _ ->
        val registrationCredentials = MockData.FIRST_REGISTRATION_CREDENTIALS
        val loginCredentials = registrationCredentials.toLoginCredentials()

        // Register the new user
        client.post("/v1/account/register") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(registrationCredentials)
        }

        val rawCookie = client.post("/v1/account/login") {
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
            setBody(loginCredentials)
        }.rawCookie()  // Contains the user's session

        // Get access to the account details without session
        val actualAccount = client.get("/v1/account") {
            rawCookie(value = rawCookie)
            bearerAuth(token = AuthUtils.NOT_STRONG_TOKEN)
        }.body<Account>()

        assertTrue {
            actualAccount.username == registrationCredentials.username &&
                    actualAccount.displayName == registrationCredentials.displayName
        }
    }
}