package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.model.CollectionsResponse
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import com.yaroslavzghoba.utils.Constants
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.getCollections(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCollectionsHandler@{
    val session = call.sessions.get<UserSession>()!!

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

    // Get the optional limit parameter if passed or default value
    val limit = call.request.queryParameters[Constants.LIMIT_PARAM_NAME]?.let { param ->
        param.toIntOrNull()?.takeIf { it >= 0 } ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "The passed query parameter \"${Constants.LIMIT_PARAM_NAME}\" must be an integer " +
                        "greater than or equal to 0.",
            )
            return@getCollectionsHandler
        }
    } ?: Constants.DEFAULT_LIMIT

    // Get the optional offset parameter if passed of default value
    val offset = call.request.queryParameters[Constants.OFFSET_PARAM_NAME]?.let { param ->
        param.toLongOrNull()?.takeIf { it >= 0 } ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "The passed query parameter \"${Constants.OFFSET_PARAM_NAME}\" must be an integer " +
                        "greater than or equal to 0.",
            )
            return@getCollectionsHandler
        }
    } ?: Constants.DEFAULT_OFFSET

    // Search collections by card id if it passed or by owner.
    val (totalCount, collections) = if (cardId != null) {

        // Return 404 if there is no card with a corresponding id in the storage
        val correspondingCard = repository.getCardById(id = cardId)
        if (correspondingCard == null) {
            val message = mapOf("message" to "There is no card with \"id\" property equal to \"$cardId\"")
            call.respond(status = HttpStatusCode.NotFound, message = message)
            return@getCollectionsHandler
        }

        // Return 403 if the corresponding card is owned by another user
        if (correspondingCard.ownerId != session.userId) {
            val message = mapOf("message" to "You cannot access someone else's card")
            call.respond(status = HttpStatusCode.Forbidden, message = message)
            return@getCollectionsHandler
        }

        // Get collections that contain the corresponding card
        repository.getCollectionsByCardId(id = cardId, limit = limit, offset = offset)
    } else {
        // Get collections that belong to the user
        repository.getCollectionsByOwnerId(id = session.userId, limit = limit, offset = offset)
    }
    val message = CollectionsResponse(totalCount = totalCount, collections = collections)
    call.respond(status = HttpStatusCode.OK, message = message)
}