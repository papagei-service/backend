package space.zghoba.routing.v1.collections

import space.zghoba.mappers.toCardCollection
import space.zghoba.model.CardCollectionUpdateRequest
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
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

    // Return 400 if the request body cannot be converted to a collection
    val body = runCatching { call.receive<CardCollectionUpdateRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
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
    if (correspondingCollection.ownerId != session!!.userId) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCollectionHandler
    }

    // Insert the collection into the storage
    val collectionToUpdate = body
        .toCardCollection(id = correspondingCollection.id, ownerId = correspondingCollection.ownerId)
    val updatedCollection = repository.updateCollection(collectionToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedCollection)
}