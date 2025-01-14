package com.yaroslavzghoba.routing.v1.collections.cards

import com.yaroslavzghoba.mappers.toCardAnswerOrNull
import com.yaroslavzghoba.model.*
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.datetime.Clock

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.Cards.webSocketCards(
    repository: Repository,
): suspend DefaultWebSocketServerSession.() -> Unit = webSocketCardsHandler@{
    val session = call.sessions.get<UserSession>()

    // Close the connection if the user is not authenticated
    if (session == null) {
        val message = "You must be authenticated using sessions to get access"
        close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        return@webSocketCardsHandler
    }

    // Close the connection if the `collection_id` parameter is not passed or is invalid
    val collectionId = call.parameters["collection_id"]?.toLongOrNull()
    if (collectionId == null) {
        val message = "The \"collection_id\" parameter is not passed or cannot be cast to number"
        close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        return@webSocketCardsHandler
    }

    // Close the connection if there is no collection with a corresponding id
    val collection = repository.getCollectionById(id = collectionId)
    if (collection == null) {
        val message = "There is no collection with \"id\" property equal to \"$collectionId\""
        close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        return@webSocketCardsHandler
    }

    // Close the connection if the user is not the owner of the collection
    if (collection.ownerId != session.userId) {
        val message = "You cannot access someone else's collection"
        close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        return@webSocketCardsHandler
    }

    // Send cards and receive answers
    var cards = sendCards(repository = repository, collectionId = collectionId)
    while (true) {
        val answer = receiveCardAnswerOrNull() ?: continue

        val sourceCard = cards.first()
        val updatedNextTimeAt = Clock.System.now() + answer.interval * (sourceCard.correctAnswersInRow + 1)

        val isCorrect = answer != CardAnswer.WRONG
        val correctAnswersInRow = sourceCard.correctAnswersInRow + if (isCorrect) 1 else 0

        val updatedCard = sourceCard.copy(nextTimeAt = updatedNextTimeAt, correctAnswersInRow = correctAnswersInRow)
        repository.updateCard(card = updatedCard)
        cards = sendCards(repository = repository, collectionId = collectionId)
    }
}

private suspend fun WebSocketServerSession.sendCards(
    repository: Repository,
    collectionId: Long,
): List<Card> {
    val cards = repository.getCardsByCollectionId(
        id = collectionId,
        sortByFirstPriority = CardSorting(column = CardSortingColumn.NEXT_TIME_AT, order = SortOrder.ASC),
        nextTimeBefore = Clock.System.now(),
        limit = 2,
        offset = 0,
    )
    sendSerialized(cards)
    return cards
}

private suspend fun WebSocketServerSession.receiveCardAnswerOrNull(): CardAnswer? =
    receiveDeserialized<CardAnswerResponse>().toCardAnswerOrNull()