package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CollectionRequest

/**
 * Converts an instance of the [CollectionRequest] class to an instance of the [CardCollection] class.
 */
@Suppress("unused")
fun CollectionRequest.toCardCollection(ownerUsername: String) = CardCollection(
    id = this.id,
    title = this.title,
    description = this.description,
    subjectType = this.subjectType,
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = ownerUsername,
)

/**
 * Converts an instance of the [CardCollection] class to an instance of the [CollectionRequest] class.
 */
@Suppress("unused")
fun CardCollection.toCollectionRequest() = CollectionRequest(
    id = this.id,
    title = this.title,
    description = this.description,
    subjectType = this.subjectType,
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
)