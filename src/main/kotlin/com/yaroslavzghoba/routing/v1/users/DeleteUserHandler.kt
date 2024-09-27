package com.yaroslavzghoba.routing.v1.users

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Users.deleteUser(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteUserHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@deleteUserHandler
    }

    // Return 404 if there is no user corresponding to the session
    val correspondingUser = repository.getUserByUsername(username = session.username)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteUserHandler
    }

    repository.deleteUserByUsername(username = correspondingUser.username)
    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}