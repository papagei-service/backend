package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.routing.RouteHandlersProvider
import io.ktor.server.response.*
import io.ktor.server.routing.*

val RouteHandlersProvider.Api.V1.getRoot: suspend RoutingContext.() -> Unit
    get() = {
        call.respond(mapOf("message" to "Hallo Papagei!"))
    }