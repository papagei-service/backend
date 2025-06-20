package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardRequest

/**
 * Converts an instance of the [CardRequest] class to an instance of the [Card] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardRequest.toCard(ownerId: Long) = Card(
    id = this.id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [Card] class to an instance of the [CardRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Card.toCardRequest() = CardRequest(
    id = this.id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)