package com.yaroslavzghoba.routing.v1.collections.cards

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.Cards.deleteCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteCardHandler@{
    val session = call.sessions.get<UserSession>()!!

    // Return 400 if the `collection_id` parameter is not passed or is invalid
    val collectionId = call.parameters["collection_id"]?.toLongOrNull()
    if (collectionId == null || collectionId <= 0) {
        val message = mapOf("message" to "The \"collection_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteCardHandler
    }

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId <= 0) {
        val message = mapOf("message" to "The \"card_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteCardHandler
    }

    // Return 404 if there is no collection with a corresponding id in the storage
    val correspondingCollection = repository.getCollectionById(id = collectionId)
    if (correspondingCollection == null) {
        val message = mapOf("message" to "There is no collection with \"id\" property equal to \"$collectionId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteCardHandler
    }

    // Return 404 if there is no card with a corresponding id in the storage
    val correspondingCard = repository.getCardById(id = cardId)
    if (correspondingCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteCardHandler
    }

    // Return 403 if the corresponding collection is owned by another user
    if (correspondingCollection.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's collection")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (correspondingCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteCardHandler
    }

    // Remove the card from the collection
    repository.removeCardFromCollection(
        card = correspondingCard,
        collection = correspondingCollection,
    )
    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}