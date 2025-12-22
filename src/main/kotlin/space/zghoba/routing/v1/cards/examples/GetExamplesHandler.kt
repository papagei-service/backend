package space.zghoba.routing.v1.cards.examples

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.Examples.getExamples(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getExamplesHandle@{
    val session = call.sessions.get<UserSession>()!!

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId < 0) {
        val message = mapOf("message" to "The \"card_id\" parameter is not passed or cannot be cast to number")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@getExamplesHandle
    }

    // Return 404 if parent card not found
    val parentCard = repository.getCardById(id = cardId)
    if (parentCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@getExamplesHandle
    }

    // Return 403 if the user is not the owner of the card
    if (parentCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@getExamplesHandle
    }

    val cards = repository.getExamplesByCardId(id = cardId, limit = 10, offset = 0)
    call.respond(status = HttpStatusCode.OK, message = cards)
}