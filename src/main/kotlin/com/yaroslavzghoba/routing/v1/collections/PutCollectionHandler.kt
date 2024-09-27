package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.model.PutCollectionRequest
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
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCollectionHandler
    }

    // Return 400 if the request body cannot be converted to a collection
    val body = try {
        call.receive<PutCollectionRequest>()
    } catch (exception: ContentTransformationException) {
        val message = mapOf("message" to "The request body cannot be converted to a card")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCollectionHandler
    }

    // Return 404 if there is no collection with a corresponding id in the storage
    val correspondingCollection = repository.getCollectionById(id = body.id)
    if (correspondingCollection == null) {
        val message = mapOf("message" to "There is no collection with \"id\" property equal to \"${body.id}\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCollectionHandler
    }

    // Return 403 if the corresponding collection is owned by another user
    if (correspondingCollection.ownerUsername != session.username) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCollectionHandler
    }

    // Insert the collection into the storage
    val collection = body.toCardCollection(ownerUsername = correspondingCollection.ownerUsername)
    repository.updateCollection(collection)

    call.respond(status = HttpStatusCode.OK, message = collection)
}