package com.yaroslavzghoba.model

import com.yaroslavzghoba.utils.SubjectType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of a request to update a card collection.
 *
 * @param title Title of the card collection.
 * @param description Description of the card collection.
 * @param subjectType The type of subject the user wants to study.
 * @param subjectLanguage The type of language the user wants to learn. Must be specified if [subjectType] is equal to
 * [SubjectType.FOREIGN_LANGUAGE].
 * @param nativeLanguage The language the user understands and wants to learn [subjectLanguage]. Must be specified if
 * [subjectType] is equal to [SubjectType.FOREIGN_LANGUAGE].
 */
@Serializable
data class PutCollectionRequest(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("subject_type") val subjectType: SubjectType,
    @SerialName("subject_language") val subjectLanguage: String?,
    @SerialName("native_language") val nativeLanguage: String?,
)