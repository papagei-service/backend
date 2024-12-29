package com.yaroslavzghoba.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a flash card for learning a unit of information.
 *
 * @param id A unique identifier of the card.
 * @param nativeLanguageValue A word or phrase in a language that the user understands.
 * @param nativeLanguageValueDescription A description of a word or phrase in a language that the user understands.
 * @param nativeLanguageValueExample An example a word or phrase in a language that the user understands.
 * @param foreignLanguageValue A word or phrase in a language that the user wants to learn.
 * @param foreignLanguageValueDescription A description of a word or phrase in a language that the user wants to learn.
 * @param foreignLanguageValueExample An example a word or phrase in a language that the user wants to learn.
 * @param nextTimeAt UTC time when you need to show the card next time.
 * @param correctAnswersInRow A number of correct answers in a row.
 * @param collectionId Unique identifier of the card collection to which the card belongs.
 */
@Serializable
data class Card(
    @SerialName("id") val id: Long?,
    @SerialName("native_language_value") val nativeLanguageValue: String,
    @SerialName("native_language_value_description") val nativeLanguageValueDescription: String?,
    @SerialName("native_language_value_example") val nativeLanguageValueExample: String?,
    @SerialName("foreign_language_value") val foreignLanguageValue: String,
    @SerialName("foreign_language_value_description") val foreignLanguageValueDescription: String?,
    @SerialName("foreign_language_value_example") val foreignLanguageValueExample: String?,
    @SerialName("next_time_at") val nextTimeAt: Instant,
    @SerialName("correct_answers_in_row") val correctAnswersInRow: Int,
    @SerialName("collection_id") val collectionId: Long,
)
