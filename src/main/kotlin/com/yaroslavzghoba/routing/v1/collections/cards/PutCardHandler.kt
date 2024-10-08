package com.yaroslavzghoba.routing.v1.collections.cards

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.model.CardRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.Cards.putCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCardHandler
    }

    // Return 404 if there is no user corresponding to the session
    val user = repository.getUserByUsername(username = session.username)
    if (user == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    // Return 400 if the `collection_id` parameter is not passed or is invalid
    val collectionId = call.parameters["collection_id"]?.toLongOrNull()
    if (collectionId == null) {
        val message = mapOf("message" to "The \"collection_id\" parameter is not passed or cannot be cast to number")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 404 if there is no collection with corresponding id
    val collection = repository.getCollectionById(id = collectionId)
    if (collection == null) {
        val message = mapOf("message" to "There is no collection with \"id\" property equal to \"$collectionId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    // Return 403 if the corresponding collection is owned by another user
    if (collection.ownerUsername != user.username) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCardHandler
    }

    // Return 400 if the request body cannot be converted to a collection
    val body = try {
        call.receive<CardRequest>()
    } catch (exception: ContentTransformationException) {
        val message = mapOf("message" to "The request body cannot be converted to a card")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 400 if the collection identifier is null
    if (body.id == null) {
        val message = mapOf("message" to "The identifier of the card to be updated cannot be null")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Insert the card into the storage
    val cardToUpdate = body.toCard(collectionId = collectionId)
    val updatedCard = try {
        repository.updateCard(cardToUpdate)
    } catch (exception: NoSuchElementException) {
        val message = mapOf("message" to "Corresponding card is not found in storage")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    call.respond(status = HttpStatusCode.OK, message = updatedCard)
}