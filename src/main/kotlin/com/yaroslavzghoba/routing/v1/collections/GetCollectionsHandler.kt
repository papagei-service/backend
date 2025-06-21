package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.getCollections(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCollectionsHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCollectionsHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCollectionsHandler
    }

    // Search collections by card id if it passed
    val cardId = call.request.queryParameters["card_id"]?.toLongOrNull()
    val collections = if (cardId != null) {
        repository.getCollectionsByCardId(id = cardId, limit = 10, offset = 0)
    } else {
        repository.getCollectionsByOwnerId(ownerId = session.userId)
    }
    call.respond(status = HttpStatusCode.NoContent, message = collections)
}