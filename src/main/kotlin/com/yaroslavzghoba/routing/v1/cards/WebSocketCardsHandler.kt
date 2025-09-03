package com.yaroslavzghoba.routing.v1.cards

import com.yaroslavzghoba.domain.HandleCardAnswerUseCase
import com.yaroslavzghoba.mappers.toDifficultyLevelOrNull
import com.yaroslavzghoba.model.*
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.datetime.Clock

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Cards.webSocketCards(
    handleCardAnswerUseCase: HandleCardAnswerUseCase,
    repository: Repository,
): suspend DefaultWebSocketServerSession.() -> Unit = webSocketCardsHandler@{
    val session = call.sessions.get<UserSession>()

    // Send cards and receive answers
    var cards = sendCards(repository = repository, ownerId = session!!.userId)
    while (true) {
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

        cards = sendCards(repository = repository, ownerId = session.userId)
    }
}

private suspend fun WebSocketServerSession.sendCards(
    repository: Repository,
    ownerId: Long,
): List<Card> {
    val (_, cards) = repository.getCardsByOwnerId(
        id = ownerId,
        sortByFirstPriority = CardSorting(column = CardSortingColumn.SHOW_NEXT_TIME_AT, order = SortOrder.ASC),
        nextTimeBefore = Clock.System.now(),
        limit = 2,
        offset = 0,
    )
    sendSerialized(cards)
    return cards
}

private suspend fun WebSocketServerSession.receiveDifficultyLevelOrNull(): CardAnswerDifficultyLevel? =
    receiveDeserialized<CardAnswerRequestWS>().toDifficultyLevelOrNull()