package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user card collection.
 *
 * @param id A unique collection identifier.
 * @param title Title of the card collection.
 * @param description Description of the card collection.
 * @param knownLanguage An ISO 639-1 code of a language the user knows.
 * @param learningLanguage An ISO 639-1 code of a language the user wants to learn.
 * @param ownerId The unique identifier of the user who owns the collection.
 */
@Serializable
data class CardCollection(
    @SerialName("id") val id: Long?,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("known_language") val knownLanguage: String?,
    @SerialName("learning_language") val learningLanguage: String?,
    @SerialName("owner_id") val ownerId: Long,
)