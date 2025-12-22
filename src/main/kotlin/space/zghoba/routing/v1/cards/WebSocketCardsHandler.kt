package space.zghoba.routing.v1.cards

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import space.zghoba.domain.HandleCardAnswerUseCase
import space.zghoba.mappers.toDifficultyLevelOrNull
import space.zghoba.model.*
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.webSocketCards(
    handleCardAnswerUseCase: HandleCardAnswerUseCase,
    repository: Repository,
): suspend DefaultWebSocketServerSession.() -> Unit = webSocketCardsHandler@{
    val session = call.sessions.get<UserSession>()!!

    // Get the optional parameter "collection_id" if passed and return 400 if it is invalid
    val collectionId: Long? = call.request.queryParameters["collection_id"]?.let { param ->
        param.toLongOrNull()?.takeIf { it > 0 } ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "The passed query parameter \"collection_id\" must be a positive integer.",
            )
            return@webSocketCardsHandler
        }
    }

    collectionId?.let { collectionId ->
        val correspondingCollection = repository.getCollectionById(id = collectionId)
        // Return 404 if there is no collection with a corresponding id.
        if (correspondingCollection == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = mapOf("message" to "There is no collection with \"id\" property equal to \"$collectionId\"")
            )
            return@webSocketCardsHandler
        }
        // Return 403 if the corresponding collection is owned by another user.
        if (correspondingCollection.ownerId != session.userId) {
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = mapOf("message" to "You cannot access someone else's collection")
            )
            return@webSocketCardsHandler
        }
    }

    val searchCardsBy = if (collectionId != null) {
        SearchCardsBy.Collection(collectionId = collectionId)
    } else {
        SearchCardsBy.User(userId = session.userId)
    }

    // Send cards and receive answers
    while (true) {
        val cards = sendCards(repository, searchCardsBy)

        // Close the connection if there are no cards to review now
        if (cards.isEmpty()) {
            val message = "There are no more cards to review at this time."
            close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        }

        // Receive the user's answer at the suggested card
        val difficultyLevel = receiveDifficultyLevelOrNull() ?: continue
        // Update the card using user's answer
        val sourceCard = cards.first()
        val cardToUpdate = handleCardAnswerUseCase.execute(card = sourceCard, difficultyLevel = difficultyLevel)
        repository.updateCard(card = cardToUpdate)
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun WebSocketServerSession.sendCards(
    repository: Repository,
    searchCardsBy: SearchCardsBy,
): List<Card> {
    val now = Clock.System.now()
    val sortings = listOf(
        CardSorting(column = CardSortingColumn.SHOW_NEXT_TIME_AT, order = SortOrder.ASC),
    )
    val limit = 2
    val offset: Long = 0

    val (_, cards) = when (searchCardsBy) {
        is SearchCardsBy.Collection -> repository.getCardsByCollectionId(
            id = searchCardsBy.collectionId,
            sortings = sortings,
            nextTimeBefore = now,
            limit = limit,
            offset = offset,
        )
        is SearchCardsBy.User -> repository.getCardsByOwnerId(
            id = searchCardsBy.userId,
            sortings = sortings,
            nextTimeBefore = now,
            limit = limit,
            offset = offset,
        )
    }

    sendSerialized(cards)
    return cards
}

private suspend fun WebSocketServerSession.receiveDifficultyLevelOrNull(): CardAnswerDifficultyLevel? =
    receiveDeserialized<CardAnswerRequestWS>().toDifficultyLevelOrNull()