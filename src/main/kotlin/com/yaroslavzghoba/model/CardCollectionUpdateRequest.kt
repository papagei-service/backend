package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the body of a request to update an example.
 *
 * @param title Title of the card collection.
 * @param description Description of the card collection.
 * @param knownLanguage An ISO code of a language the user understands.
 * @param learningLanguage An ISO code of a language the user wants to learn.
 */
@Serializable
data class CardCollectionUpdateRequest(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("native_language") val knownLanguage: String?,
    @SerialName("learning_language") val learningLanguage: String?,
)