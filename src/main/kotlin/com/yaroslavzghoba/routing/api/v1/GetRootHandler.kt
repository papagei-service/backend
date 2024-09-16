package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.routing.RouteHandlersProvider
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RouteHandlersProvider.Api.V1.getRoot(): suspend RoutingContext.() -> Unit = {
    val message = mapOf("message" to "Hallo Papagei!")
    call.respond(message)
}