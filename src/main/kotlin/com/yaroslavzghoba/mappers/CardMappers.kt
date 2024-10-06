package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardRequest

/**
 * Converts an instance of the [CardRequest] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun CardRequest.toCard(collectionId: Long) = Card(
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

/**
 * Converts an instance of the [Card] class to an instance of the [CardRequest] class.
 */
@Suppress("unused")
fun Card.toCardRequest() = CardRequest(
    id = this.id,
    frontTitle = this.frontTitle,
    frontDescription = this.frontDescription,
    frontExample = this.frontExample,
    backTitle = this.backTitle,
    backDescription = this.backDescription,
    backExample = this.backExample,
    nextTimeAt = this.nextTimeAt,
    currentIntervalMs = this.currentIntervalMs,
)