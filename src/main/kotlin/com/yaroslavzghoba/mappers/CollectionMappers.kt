package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CardCollectionRequest

/**
 * Converts an instance of the [CardCollectionRequest] class to an instance of the [CardCollection] class.
 */
@Suppress("unused")
fun CardCollectionRequest.toCardCollection(ownerId: Long) = CardCollection(
    id = this.id,
    title = this.title,
    description = this.description,
    nativeLanguageISOCode = this.nativeLanguageISOCode,
    foreignLanguageISOCode = this.foreignLanguageISOCode,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [CardCollection] class to an instance of the [CardCollectionRequest] class.
 */
@Suppress("unused")
fun CardCollection.toCollectionRequest() = CardCollectionRequest(
    id = this.id,
    title = this.title,
    description = this.description,
    nativeLanguageISOCode = this.nativeLanguageISOCode,
    foreignLanguageISOCode = this.foreignLanguageISOCode,
)