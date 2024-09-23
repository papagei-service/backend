package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.model.CardCollectionDbo
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.utils.SubjectType

/**
 * Converts an instance of the [CardCollectionDbo] class to an instance of the [CardCollection] class.
 */
@Suppress("unused")
fun CardCollectionDbo.toCardCollection() = CardCollection(
    id = this.id,
    title = this.title,
    description = this.description,
    subjectType = SubjectType.valueOf(this.subjectType),
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = this.ownerUsername,
)

/**
 * Converts an instance of the [CardCollection] class to an instance of the [CardCollectionDbo] class.
 */
@Suppress("unused")
fun CardCollection.toCardCollectionDbo() = CardCollectionDbo(
    id = this.id,
    title = this.title,
    description = this.description,
    subjectType = this.subjectType.name,
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = this.ownerUsername,
)