package com.yaroslavzghoba.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, _ ->
            val message = mapOf("message" to "Unexpected error on the server side")
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = message,
            )
        }
    }
}