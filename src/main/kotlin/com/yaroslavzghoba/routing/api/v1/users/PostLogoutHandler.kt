package com.yaroslavzghoba.routing.api.v1.users

import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun RouteHandlersProvider.Api.V1.Users.postLogout(

): suspend RoutingContext.() -> Unit = postLogoutHandler@{
    call.sessions.clear<UserSession>()

    val message = mapOf("message" to "The user session was successfully deleted")
    call.respond(status = HttpStatusCode.OK, message = message)
}