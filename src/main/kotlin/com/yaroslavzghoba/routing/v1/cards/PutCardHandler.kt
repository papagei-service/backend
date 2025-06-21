package com.yaroslavzghoba.routing.v1.cards

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
fun RouteHandlersProvider.V1.Cards.putCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCardHandler
    }

    // Return 400 if the request body cannot be converted to a card
    val body = runCatching { call.receive<CardRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 400 if the collection identifier is null
    if (body.id == null) {
        val message = mapOf("message" to "The identifier of the card to be updated cannot be null")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 404 if there is no card with a corresponding id in the storage
    val correspondingCard = repository.getCardById(id = body.id)
    if (correspondingCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"${body.id}\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (correspondingCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCardHandler
    }

    // Insert the collection into the storage
    val cardToUpdate = body.toCard(ownerId = correspondingCard.ownerId)
    val updatedCard = repository.updateCard(cardToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedCard)
}