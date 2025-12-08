package space.zghoba.routing.v1.cards

import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.deleteCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId <= 0) {
        val message = mapOf("message" to "The \"card_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteCardHandler
    }

    // Return 404 if there is no card with a corresponding id in the storage
    val card = repository.getCardById(id = cardId)
    if (card == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteCardHandler
    }

    // Return 403 if the corresponding card is owned by another user
    if (card.ownerId != session!!.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteCardHandler
    }

    repository.deleteCardById(id = cardId)

    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}