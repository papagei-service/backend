package space.zghoba.routing.v1.cards

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.logging.*
import space.zghoba.mappers.toCard
import space.zghoba.model.CardUpdateRequest
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession

@Suppress("unused")
private val LOGGER =
    KtorSimpleLogger(RouteHandlersProvider.V1.Cards::putCard.javaClass.packageName)

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.putCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putCardHandler@{
    val session = call.sessions.get<UserSession>()

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

    // Return 404 if there is no card with a corresponding id in the storage
    val correspondingCard = repository.getCardById(id = cardId)
    if (correspondingCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (correspondingCard.ownerId != session!!.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putCardHandler
    }

    // Insert the collection into the storage
    val cardToUpdate = body
        .toCard(id = correspondingCard.id, ownerId = correspondingCard.ownerId)
    val updatedCard = repository.updateCard(cardToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedCard)
}