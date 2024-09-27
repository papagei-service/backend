package com.yaroslavzghoba.routing.v1

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.getCollections(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCollectionsHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCollectionsHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserByUsername(username = session.username)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCollectionsHandler
    }

    val collections = repository
        .getCollectionsByOwnerUsername(ownerUsername = correspondingUser.username)
    call.respond(status = HttpStatusCode.NoContent, message = collections)
}