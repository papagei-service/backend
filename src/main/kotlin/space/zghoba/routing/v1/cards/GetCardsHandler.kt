package space.zghoba.routing.v1.cards

import space.zghoba.model.CardSorting
import space.zghoba.model.CardsResponse
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import space.zghoba.utils.Constants
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.Instant

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.getCards(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getCardsHandler@{
    val session = call.sessions.get<UserSession>()!!

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

    val sortings = (call.request.queryParameters[Constants.SORT_BY_PARAM_NAME] ?: Constants.DEFAULT_SORT_BY)
        ?.split(Constants.SORTING_DELIMITER)
        ?.map { string ->
            val cardSorting = CardSorting.fromStringOrNull(string = string)
            if (cardSorting == null) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "The passed query parameter \"${Constants.SORT_BY_PARAM_NAME}\" is invalid.",
                )
                return@getCardsHandler
            }
            cardSorting
        }
        ?: emptyList()

    val nextTimeBefore = (call.request.queryParameters[Constants.NEXT_TIME_BEFORE_PARAM_NAME]
        ?: Constants.DEFAULT_NEXT_TIME_BEFORE)
        ?.let { string ->
            try {
                Instant.parse(string)
            } catch (_: IllegalArgumentException) {
                val message = "The value \"$string\" passed to the \"${Constants.NEXT_TIME_BEFORE_PARAM_NAME}\" " +
                        "query parameter is invalid."
                call.respond(status = HttpStatusCode.BadRequest, message = message)
                return@getCardsHandler
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
            return@getCardsHandler
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
            return@getCardsHandler
        }
    } ?: Constants.DEFAULT_OFFSET

    // Search cards by collection id if it passed or by owner.
    val (totalCount, cards) = if (collectionId != null) {

        // Return 404 if there is no collection with a corresponding id in the storage
        val correspondingCollection = repository.getCollectionById(id = collectionId)
        if (correspondingCollection == null) {
            val message = mapOf("message" to "There is no collection with \"id\" property equal to \"$collectionId\"")
            call.respond(status = HttpStatusCode.NotFound, message = message)
            return@getCardsHandler
        }

        // Return 403 if the corresponding collection is owned by another user
        if (correspondingCollection.ownerId != session.userId) {
            val message = mapOf("message" to "You cannot access someone else's collection")
            call.respond(status = HttpStatusCode.Forbidden, message = message)
            return@getCardsHandler
        }

        // Get cards that belong to the corresponding collection
        repository.getCardsByCollectionId(
            id = collectionId,
            sortings = sortings,
            nextTimeBefore = nextTimeBefore,
            limit = limit,
            offset = offset,
        )
    } else {
        // Get cards that belong to the user
        repository.getCardsByOwnerId(
            id = session.userId,
            sortings = sortings,
            nextTimeBefore = nextTimeBefore,
            limit = limit,
            offset = offset,
        )
    }
    val message = CardsResponse(totalCount = totalCount, cards = cards)
    call.respond(status = HttpStatusCode.OK, message = message)
}