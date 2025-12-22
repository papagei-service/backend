package space.zghoba.routing

import io.ktor.client.request.*
import io.ktor.http.*
import space.zghoba.utils.testConfiguredApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingTest {

    @Test
    fun `Receive 500 status code during error on server side`() = testConfiguredApplication { client, _ ->
        val response0 = client.get("/error")
        assertEquals(
            expected = HttpStatusCode.Companion.InternalServerError,
            actual = response0.status
        )
    }
}