package space.zghoba.mappers

import space.zghoba.model.Card
import space.zghoba.model.CardInsertRequest
import space.zghoba.model.CardUpdateRequest
import kotlin.time.ExperimentalTime

/**
 * Converts an instance of the [CardInsertRequest] class to an instance of the [Card] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun CardInsertRequest.toCard(id: Long?, ownerId: Long) = Card(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [CardInsertRequest] class to an instance of the [CardUpdateRequest] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun CardInsertRequest.toCardUpdateRequest() = CardUpdateRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)

/**
 * Converts an instance of the [CardUpdateRequest] class to an instance of the [Card] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun CardUpdateRequest.toCard(id: Long?, ownerId: Long) = Card(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [CardUpdateRequest] class to an instance of the [CardInsertRequest] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun CardUpdateRequest.toCardInsertRequest() = CardInsertRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)

/**
 * Converts an instance of the [Card] class to an instance of the [CardInsertRequest] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun Card.toCardInsertRequest() = CardInsertRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)

/**
 * Converts an instance of the [Card] class to an instance of the [CardUpdateRequest] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun Card.toCardUpdateRequest() = CardUpdateRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)