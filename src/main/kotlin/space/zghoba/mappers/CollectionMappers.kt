package space.zghoba.mappers

import space.zghoba.model.CardCollection
import space.zghoba.model.CardCollectionInsertRequest
import space.zghoba.model.CardCollectionUpdateRequest

/**
 * Converts an instance of the [CardCollectionInsertRequest] class to an instance of the [CardCollection] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollectionInsertRequest.toCardCollection(id: Long?, ownerId: Long) = CardCollection(
    id = id,
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [CardCollectionInsertRequest] class
 * to an instance of the [CardCollectionUpdateRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollectionInsertRequest.toCardCollectionUpdateRequest() = CardCollectionUpdateRequest(
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
)

/**
 * Converts an instance of the [CardCollectionUpdateRequest] class to an instance of the [CardCollection] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollectionUpdateRequest.toCardCollection(id: Long?, ownerId: Long) = CardCollection(
    id = id,
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
    ownerId = ownerId,
)

/**
 * Converts an instance of the [CardCollectionUpdateRequest] class
 * to an instance of the [CardCollectionInsertRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollectionUpdateRequest.toCardCollectionInsertRequest() = CardCollectionInsertRequest(
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
)

/**
 * Converts an instance of the [CardCollection] class to an instance of the [CardCollectionInsertRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollection.toCollectionInsertRequest() = CardCollectionInsertRequest(
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
)

/**
 * Converts an instance of the [CardCollection] class to an instance of the [CardCollectionUpdateRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardCollection.toCollectionUpdateRequest() = CardCollectionUpdateRequest(
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
)