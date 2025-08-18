package com.yaroslavzghoba.model

/**
 * Represents an answer to a single card that sent by a client using HTTP-requests.
 *
 * @param cardId The unique identifier of the card to which the answer is assigned.
 * @param difficultyLevel A name of the one of [CardAnswerDifficultyLevel].
 */
data class CardAnswerRequest(
    val cardId: Long,
    val difficultyLevel: String,
)