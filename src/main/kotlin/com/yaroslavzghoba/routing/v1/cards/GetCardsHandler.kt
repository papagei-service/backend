package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.getCards(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCardsHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "User session is missing, invalid or expired")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCardsHandler
    }

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getCardsHandler
    }

    // Search cards by collection id if it passed
    val collectionId = call.request.queryParameters["collection_id"]?.toLongOrNull()
    val cards = if (collectionId != null) {
        repository.getCardsByCollectionId(id = collectionId, limit = 10, offset = 0)
    } else {
        repository.getCardsByOwnerId(id = session.userId)
    }
    call.respond(status = HttpStatusCode.OK, message = cards)
}