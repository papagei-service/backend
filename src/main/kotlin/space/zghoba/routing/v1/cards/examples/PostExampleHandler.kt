package space.zghoba.routing.v1.cards.examples

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.mappers.toExample
import space.zghoba.model.ExampleInsertRequest
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.Examples.postExample(
    repository: Repository,
): suspend RoutingContext.() -> Unit = postExampleHandler@{
    val session = call.sessions.get<UserSession>()!!

    // Return 400 if the request body cannot be converted to an example
    val body = runCatching { call.receive<ExampleInsertRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to an example")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postExampleHandler
    }

    // Return 400 if the `card_id` parameter is not passed or is invalid
    val cardId = call.parameters["card_id"]?.toLongOrNull()
    if (cardId == null || cardId < 0) {
        val message = mapOf("message" to "The \"card_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postExampleHandler
    }

    // Return 404 if parent card not found
    val parentCard = repository.getCardById(id = cardId)
    if (parentCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@postExampleHandler
    }

    // Return 403 if the user is not the owner of the card
    if (parentCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@postExampleHandler
    }

    // Insert the card into the storage
    val exampleToInsert = body.toExample(id = null, cardId = cardId)
    val insertedCard = repository.insertExample(exampleToInsert)

    call.respond(status = HttpStatusCode.Created, message = insertedCard)
}