package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.model.CardCollectionUpdateRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.putCollection(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putCollectionHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCollectionHandler
    }

    // Return 400 if the request body cannot be converted to a collection
    val body = runCatching { call.receive<CardCollectionUpdateRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCollectionHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCollectionHandler
    }


    // Return 400 if the `collection_id` parameter is not passed or is invalid
    val collectionId = call.parameters["collection_id"]?.toLongOrNull()
    if (collectionId == null || collectionId <= 0) {
        val message = mapOf("message" to "The \"collection_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCollectionHandler
    }

    // Return 404 if there is no collection with a corresponding id in the storage
    val correspondingCollection = repository.getCollectionById(id = collectionId)
    if (correspondingCollection == null) {
        val message = mapOf("message" to "There is no collection with \"id\" property equal to \"${collectionId}\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCollectionHandler
    }

    // Return 403 if the corresponding collection is owned by another user
    if (correspondingCollection.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCollectionHandler
    }

    // Insert the collection into the storage
    val collectionToUpdate = body.toCardCollection(id = collectionId, ownerId = correspondingCollection.ownerId)
    val updatedCollection = repository.updateCollection(collectionToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedCollection)
}