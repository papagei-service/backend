package space.zghoba.routing.v1.examples

import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Examples.deleteExample(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteExampleHandler@{
    val session = call.sessions.get<UserSession>()!!

    // Return 400 if the `example_id` parameter is not passed or is invalid
    val exampleId = call.parameters["example_id"]?.toLongOrNull()
    if (exampleId == null || exampleId < 0) {
        val message = mapOf("message" to "The \"example_id\" parameter must be a positive integer.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@deleteExampleHandler
    }

    // Return 404 if there is no example with a corresponding id
    val example = repository.getExampleById(id = exampleId)
    if (example == null) {
        val message = mapOf("message" to "There is no example with \"id\" property equal to \"$exampleId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@deleteExampleHandler
    }

    // Return 403 if the user is not the owner of the card
    val parentCard = repository.getCardById(id = example.cardId)!!
    if (parentCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@deleteExampleHandler
    }

    repository.deleteExampleById(id = exampleId)

    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}