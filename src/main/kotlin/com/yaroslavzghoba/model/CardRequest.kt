package com.yaroslavzghoba.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the body of a request to insert or update a card.
 *
 * @param id A unique identifier of the card.
 * @param knownLanguageText A word or phrase in a language that the user knows.
 * @param learningLanguageText A word or phrase in a language that the user wants to learn.
 * @param notes Additional notes on a word or phrase.
 * @param lastAnsweredAt UTC time when the user last answered to the card.
 * @param showNextTimeAt UTC time when the card should be shown next time.
 * @param correctAnswersInRow A number of correct answers in a row.
 */
@Serializable
data class CardRequest(
    @SerialName("id") val id: Long?,
    @SerialName("known_language_text") val knownLanguageText: String,
    @SerialName("learning_language_text") val learningLanguageText: String,
    @SerialName("notes") val notes: String?,
    @SerialName("last_answered_at") val lastAnsweredAt: Instant?,
    @SerialName("show_next_time_at") val showNextTimeAt: Instant?,
    @SerialName("correct_answers_in_row") val correctAnswersInRow: Int,
)