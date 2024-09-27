package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.PostCardRequest
import com.yaroslavzghoba.model.PutCardRequest

/**
 * Converts an instance of the [PostCardRequest] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun PostCardRequest.toCard(id: Long?, collectionId: Long) = Card(
    id = id,
    frontTitle = this.frontTitle,
    frontDescription = this.frontDescription,
    frontExample = this.frontExample,
    backTitle = this.backTitle,
    backDescription = this.backDescription,
    backExample = this.backExample,
    nextTimeAt = this.nextTimeAt,
    currentIntervalMs = this.currentIntervalMs,
    collectionId = collectionId,
)

/**
 * Converts an instance of the [PostCardRequest] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun PutCardRequest.toCard(collectionId: Long) = Card(
    id = this.id,
    frontTitle = this.frontTitle,
    frontDescription = this.frontDescription,
    frontExample = this.frontExample,
    backTitle = this.backTitle,
    backDescription = this.backDescription,
    backExample = this.backExample,
    nextTimeAt = this.nextTimeAt,
    currentIntervalMs = this.currentIntervalMs,
    collectionId = collectionId,
)