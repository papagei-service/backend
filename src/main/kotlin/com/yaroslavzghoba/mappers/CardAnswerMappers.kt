package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.CardAnswer
import com.yaroslavzghoba.model.CardAnswerResponse

/**
 * Get the enum constant of the [CardAnswer] corresponding to the client's answer or null if it can't be matched.
 *
 * @receiver A response with an answer sent by a client.
 * @return A corresponding enum constant of the [CardAnswer].
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardAnswerResponse.toCardAnswerOrNull(): CardAnswer? =
    this.answer.toCardAnswerOrNull()

/**
 * Get the enum constant of the [CardAnswer] with the corresponding name or null if it can't be matched.
 *
 * @receiver A name of the answer to a card.
 * @return A corresponding enum constant of the [CardAnswer].
 */
@Suppress("unused", "nothing_to_inline")
inline fun String.toCardAnswerOrNull(): CardAnswer? {
    return try {
        CardAnswer.valueOf(value = this)
    } catch (exception: IllegalArgumentException) {
        null
    }
}