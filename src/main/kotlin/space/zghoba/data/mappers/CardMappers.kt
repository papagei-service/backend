package space.zghoba.data.mappers

import space.zghoba.data.local.dao.CardDao
import space.zghoba.model.Card
import kotlin.time.ExperimentalTime

/**
 * Converts an instance of the [CardDao] class to an instance of the [Card] class.
 */
@OptIn(ExperimentalTime::class)
@Suppress("unused", "nothing_to_inline")
inline fun CardDao.toCard() = Card(
    id = this.id.value,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    notes = this.notes,
    lastAnsweredAt = this.lastAnsweredAt,
    showNextTimeAt = this.showNextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    ownerId = this.ownerId.id.value,
)