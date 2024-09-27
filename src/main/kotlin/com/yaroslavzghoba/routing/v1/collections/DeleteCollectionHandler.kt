package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.deleteCollection(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteCollectionHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@deleteCollectionHandler
    }

    // Return 400 if the `collection_id` parameter is not passed or is invalid
    val collectionId = call.parameters["collection_id"]?.toLongOrNull()
    if (collectionId == null) {
        val message = mapOf("message" to "The \"collection_id\" parameter is not passed or cannot be cast to number")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteCollectionHandler
    }

    // Return 404 if there is no collection with a corresponding id in the storage
    val collection = repository.getCollectionById(id = collectionId)
    if (collection == null) {
        val message = mapOf("message" to "There is no collection with \"id\" property equal to \"$collectionId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteCollectionHandler
    }

    // Return 403 if the corresponding collection is owned by another user
    if (collection.ownerUsername != session.username) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteCollectionHandler
    }

    repository.deleteCollectionById(id = collectionId)

    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}