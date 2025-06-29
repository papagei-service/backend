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

    // Get the optional parameter "collection_id" if passed and return 400 if it is invalid
    val collectionId: Long? = call.request.queryParameters["collection_id"]?.let { param ->
        param.toLongOrNull()?.takeIf { it > 0 } ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "The passed query parameter \"collection_id\" must be a positive integer.",
            )
            return@getCardsHandler
        }
    }

    // Search cards by collection id if it passed or by owner.
    val cards = if (collectionId != null) {
        repository.getCardsByCollectionId(id = collectionId, limit = 10, offset = 0)
    } else {
        repository.getCardsByOwnerId(id = session!!.userId)
    }
    call.respond(status = HttpStatusCode.OK, message = cards)
}