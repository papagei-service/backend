package space.zghoba.routing.v1.examples

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.mappers.toExample
import space.zghoba.model.Card
import space.zghoba.model.ExampleUpdateRequest
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Examples.putExample(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putExampleHandler@{
    val session = call.sessions.get<UserSession>()!!

    // Return 400 if the request body cannot be converted to an example
    val body = runCatching { call.receive<ExampleUpdateRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to an example")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putExampleHandler
    }

    // Return 400 if the `example_id` parameter is not passed or is invalid
    val exampleId = call.parameters["example_id"]?.toLongOrNull()
    if (exampleId == null || exampleId < 0) {
        val message = mapOf("message" to "The \"example_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@putExampleHandler
    }

    // Return 404 if there is no example with a corresponding id
    val correspondingExample = repository.getExampleById(id = exampleId)
    if (correspondingExample == null) {
        val message = mapOf("message" to "There is no example with \"id\" property equal to \"$exampleId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putExampleHandler
    }

    // Return 403 if the user is not the owner of the new parent card
    val currentParentCard = repository.getCardById(id = correspondingExample.cardId)!!
    if (currentParentCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putExampleHandler
    }

    // Validate a new parent card if it is different from the current one
    val parentCard = if (body.cardId != correspondingExample.cardId) {
        val newParentCard = validateNewParentCard(
            repository = repository,
            newParentCardId = body.cardId,
            userId = session.userId,
        )
        newParentCard ?: return@putExampleHandler
    } else currentParentCard

    // Update the example in the storage
    val exampleToUpdate = body.toExample(id = exampleId).copy(cardId = parentCard.id!!)
    val updatedExample = repository.updateExample(exampleToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedExample)
}

private suspend fun RoutingContext.validateNewParentCard(
    repository: Repository,
    newParentCardId: Long,
    userId: Long,
): Card? {
    val newParentCard = repository.getCardById(id = newParentCardId)

    // Return 404 if there is no card with "id" equals to passed "card_id"
    if (newParentCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to passed \"$newParentCardId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return null
    }

    // Return 403 if the user is not the owner of the new parent card
    if (newParentCard.ownerId != userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return null
    }

    return newParentCard
}