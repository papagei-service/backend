package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.CardAnswerDifficultyLevel
import com.yaroslavzghoba.model.CardAnswerRequestWS

/**
 * Get the enum constant of the [CardAnswerDifficultyLevel] corresponding to the client's answer
 * or null if it can't be matched.
 *
 * @receiver A response with an answer sent by a client.
 * @return A corresponding enum constant of the [CardAnswerDifficultyLevel].
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardAnswerRequestWS.toDifficultyLevelOrNull(): CardAnswerDifficultyLevel? =
    this.difficultyLevel.toDifficultyLevelOrNull()

/**
 * Get the enum constant of the [CardAnswerDifficultyLevel] with the corresponding name or null if it can't be matched.
 *
 * @receiver A name of the answer to a card.
 * @return A corresponding enum constant of the [CardAnswerDifficultyLevel].
 */
@Suppress("unused", "nothing_to_inline")
inline fun String.toDifficultyLevelOrNull(): CardAnswerDifficultyLevel? {
    return try {
        CardAnswerDifficultyLevel.valueOf(value = this)
    } catch (exception: IllegalArgumentException) {
        null
    }
}