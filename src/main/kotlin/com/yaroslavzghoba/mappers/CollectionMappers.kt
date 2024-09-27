package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.PostCollectionRequest
import com.yaroslavzghoba.model.PutCollectionRequest

/**
 * Converts an instance of the [PostCollectionRequest] class to an instance of the [Card] class.
 */
fun PostCollectionRequest.toCardCollection(id: Long?, ownerUsername: String) = CardCollection(
    id = id,
    title = this.title,
    description = this.description,
    subjectType = this.subjectType,
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = ownerUsername,
)

/**
 * Converts an instance of the [PutCollectionRequest] class to an instance of the [Card] class.
 */
fun PutCollectionRequest.toCardCollection(ownerUsername: String) = CardCollection(
    id = this.id,
    title = this.title,
    description = this.description,
    subjectType = this.subjectType,
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = ownerUsername,
)