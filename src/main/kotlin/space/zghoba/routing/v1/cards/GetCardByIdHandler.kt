package space.zghoba.routing.v1.cards

import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.getCardById(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCardByIdHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId < 0) {
        val message = mapOf("message" to "The \"card_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@getCardByIdHandler
    }

    // Return 404 if there is no card with a corresponding id
    val card = repository.getCardById(id = cardId)
    if (card == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@getCardByIdHandler
    }

    // Return 403 if the user is not the owner of the card
    if (card.ownerId != session!!.userId) {
        val message = mapOf("message" to "You cannot access someone else's card")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@getCardByIdHandler
    }

    call.respond(status = HttpStatusCode.OK, message = card)
}