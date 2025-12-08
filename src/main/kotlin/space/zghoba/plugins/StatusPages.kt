package space.zghoba.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.logging.*

private val LOGGER = KtorSimpleLogger("space.zghoba.plugins")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            LOGGER.error("Unexpected error on the server side. Sending HTTP code 500 in response", throwable)

            val message = mapOf("message" to "Unexpected error on the server side")
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = message,
            )
        }
    }
}