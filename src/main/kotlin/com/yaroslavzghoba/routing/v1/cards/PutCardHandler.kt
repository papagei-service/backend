package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.model.CardUpdateRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.logging.KtorSimpleLogger

@Suppress("unused")
private val LOGGER =
    KtorSimpleLogger(RouteHandlersProvider.V1.Cards::putCard.javaClass.packageName)

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
    val body = runCatching { call.receive<CardUpdateRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId <= 0) {
        val message = mapOf("message" to "The \"card_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putCardHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putCardHandler
    }

    // Return 404 if there is no card with a corresponding id in the storage
    val correspondingCard = repository.getCardById(id = cardId)
    if (correspondingCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (correspondingCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCardHandler
    }

    LOGGER.debug("Card insert request. All stages of validation have been passed.")
    LOGGER.debug("The owner's ID: ${session.userId}")

    // Insert the collection into the storage
    val cardToUpdate = body
        .toCard(id = correspondingCard.id, ownerId = correspondingCard.ownerId)
    val updatedCard = repository.updateCard(cardToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedCard)
}