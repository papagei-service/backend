package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the body of a request to insert or update a collection.
 *
 * @param id A unique collection identifier.
 * @param title Title of the card collection.
 * @param description Description of the card collection.
 * @param nativeLanguageISOCode An ISO code of a language the user understands.
 * @param foreignLanguageISOCode An ISO code of a language the user wants to learn.
 */
@Serializable
data class CardCollectionRequest(
    @SerialName("id") val id: Long?,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("native_language_iso_639_1") val nativeLanguageISOCode: String?,
    @SerialName("foreign_language_iso_639_1") val foreignLanguageISOCode: String?,
)