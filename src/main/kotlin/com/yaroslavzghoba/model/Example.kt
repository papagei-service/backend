package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a card learning material usage example.
 *
 * @param id The unique identifier of the example.
 * @param knownLanguageText Example of usage in a language the user knows.
 * @param learningLanguageText Example of usage in a language the user want to learn.
 * @param cardId Unique identifier of the card to which the example belongs.
 */
@Serializable
data class Example(
    @SerialName("id") val id: Long?,
    @SerialName("known_language_text") val knownLanguageText: String,
    @SerialName("learning_language_text") val learningLanguageText: String,
    @SerialName("card_id") val cardId: Long,
)