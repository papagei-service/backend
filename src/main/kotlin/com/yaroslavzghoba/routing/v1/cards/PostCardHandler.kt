package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.model.CardRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.postCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = postCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postCardHandler
    }

    // Return 400 if the request body cannot be converted to a card
    val body = runCatching { call.receive<CardRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a card")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCardHandler
    }

    // Return 404 if there is no user corresponding to the session
    val user = repository.getUserById(id = session.userId)
    if (user == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@postCardHandler
    }

    // Insert the card into the storage
    val cardToInsert = body.toCard(ownerId = user.id!!)
    val insertedCard = try {
        repository.insertCard(cardToInsert)
    } catch (_: ExposedSQLException) {
        val message = mapOf("message" to "A card with the corresponding identifier already exists")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCardHandler
    }

    call.respond(status = HttpStatusCode.Created, message = insertedCard)
}