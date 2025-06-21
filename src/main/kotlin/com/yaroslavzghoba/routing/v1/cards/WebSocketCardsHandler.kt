package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.mappers.toCardAnswerOrNull
import com.yaroslavzghoba.model.*
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.datetime.Clock

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.webSocketCards(
    repository: Repository,
): suspend DefaultWebSocketServerSession.() -> Unit = webSocketCardsHandler@{
    val session = call.sessions.get<UserSession>()

    // Close the connection if the user is not authenticated
    if (session == null) {
        val message = "User session is missing, invalid or expired"
        close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        return@webSocketCardsHandler
    }

    // Send cards and receive answers
    var cards = sendCards(repository = repository, ownerId = session.userId)
    while (true) {
        // Close the connection if there are no cards to review now
        if (cards.isEmpty()) {
            val message = "There are no more cards to review at this time."
            close(CloseReason(code = CloseReason.Codes.NORMAL, message = message))
        }

        // Receive the user's answer at the suggested card
        val answer = receiveCardAnswerOrNull() ?: continue

        val sourceCard = cards.first()
        val updatedNextTimeAt = Clock.System.now() + answer.interval * (sourceCard.correctAnswersInRow + 1)

        val isCorrect = answer != CardAnswer.WRONG
        val correctAnswersInRow = sourceCard.correctAnswersInRow + if (isCorrect) 1 else 0

        val updatedCard = sourceCard.copy(showNextTimeAt = updatedNextTimeAt, correctAnswersInRow = correctAnswersInRow)
        repository.updateCard(card = updatedCard)
        cards = sendCards(repository = repository, ownerId = session.userId)
    }
}

private suspend fun WebSocketServerSession.sendCards(
    repository: Repository,
    ownerId: Long,
): List<Card> {
    val cards = repository.getCardsByOwnerId(
        id = ownerId,
        sortByFirstPriority = CardSorting(column = CardSortingColumn.SHOW_NEXT_TIME_AT, order = SortOrder.ASC),
        nextTimeBefore = Clock.System.now(),
        limit = 2,
        offset = 0,
    )
    sendSerialized(cards)
    return cards
}

private suspend fun WebSocketServerSession.receiveCardAnswerOrNull(): CardAnswer? =
    receiveDeserialized<CardAnswerResponse>().toCardAnswerOrNull()