package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.getCollections(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCollectionsHandler@{
    val session = call.sessions.get<UserSession>()

    // Get the optional parameter "card_id" if passed and return 400 if it is invalid
    val cardId: Long? = call.request.queryParameters["card_id"]?.let { param ->
        param.toLongOrNull()?.takeIf { it > 0 } ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "The passed query parameter \"card_id\" must be a positive integer.",
            )
            return@getCollectionsHandler
        }
    }

    // Search collections by card id if it passed or by owner.
    val collections = if (cardId != null) {
        repository.getCollectionsByCardId(id = cardId, limit = 10, offset = 0)
    } else {
        repository.getCollectionsByOwnerId(ownerId = session!!.userId)
    }
    call.respond(status = HttpStatusCode.OK, message = collections)
}