package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.deleteCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@deleteCardHandler
    }

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val card_id = call.parameters["card_id"]?.toLongOrNull()
    if (card_id == null) {
        val message = mapOf("message" to "The \"card_id\" parameter is not passed or cannot be cast to number")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteCardHandler
    }

    // Return 404 if there is no card with a corresponding id in the storage
    val card = repository.getCardById(id = card_id)
    if (card == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$card_id\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (card.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteCardHandler
    }

    repository.deleteCollectionById(id = card_id)

    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}