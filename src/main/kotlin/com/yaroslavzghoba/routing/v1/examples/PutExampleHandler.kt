package com.yaroslavzghoba.routing.v1.examples

import com.yaroslavzghoba.mappers.toExample
import com.yaroslavzghoba.model.ExampleToUpdateRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Examples.putExample(
    repository: Repository,
): suspend RoutingContext.() -> Unit = putExampleHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putExampleHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@putExampleHandler
    }

    // Return 400 if the request body cannot be converted to an example
    val body = runCatching { call.receive<ExampleToUpdateRequest>() }.getOrNull()
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
    val example = repository.getExampleById(id = exampleId)
    if (example == null) {
        val message = mapOf("message" to "There is no example with \"id\" property equal to \"$exampleId\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putExampleHandler
    }

    // Return 404 if parent card not found
    val parentCard = repository.getCardById(id = example.cardId)
    if (parentCard == null) {
        val message = mapOf("message" to "There is no card with \"id\" property equal to \"${example.cardId}\"")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@putExampleHandler
    }

    // Return 403 if the user is not the owner of the card
    if (parentCard.ownerId != session.userId) {
        val message = mapOf("message" to "You cannot access someone else's example")
        call.respond(status = HttpStatusCode.Forbidden, message = message)
        return@putExampleHandler
    }

    // Update the example in the storage
    val exampleToUpdate = body.toExample(id = exampleId)
    val updatedExample = repository.updateExample(exampleToUpdate)

    call.respond(status = HttpStatusCode.OK, message = updatedExample)
}