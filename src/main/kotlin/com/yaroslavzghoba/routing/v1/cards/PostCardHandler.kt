package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.model.CardInsertRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.logging.*

@Suppress("unused")
private val LOGGER =
    KtorSimpleLogger(RouteHandlersProvider.V1.Cards::postCard.javaClass.packageName)

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.postCard(
    repository: Repository,
): suspend RoutingContext.() -> Unit = postCardHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 400 if the request body cannot be converted to a card
    val body = runCatching { call.receive<CardInsertRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a card")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCardHandler
    }

    // Insert the card into the storage
    val cardToInsert = body.toCard(id = null, ownerId = session!!.userId)
    val insertedCard = repository.insertCard(card = cardToInsert)

    call.respond(status = HttpStatusCode.Created, message = insertedCard)
}