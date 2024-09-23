package com.yaroslavzghoba.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user card collection object that can be stored in the database.
 *
 * @param id A unique collection identifier.
 * @param title Title of the card collection.
 * @param description Description of the card collection.
 * @param subjectType The type of subject the user wants to study.
 * @param subjectLanguage The type of language the user wants to learn. Must be specified if [subjectType] is equal to
 * "FOREIGN_LANGUAGE".
 * @param nativeLanguage The language the user understands and wants to learn [subjectLanguage]. Must be specified if
 * [subjectType] is equal to "FOREIGN_LANGUAGE".
 * @param ownerUsername Unique identifier of the user who owns the collection.
 */
@Serializable
data class CardCollectionDbo(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("subject_type") val subjectType: String,
    @SerialName("subject_language") val subjectLanguage: String?,
    @SerialName("native_language") val nativeLanguage: String?,
    @SerialName("owner_username") val ownerUsername: String,
)