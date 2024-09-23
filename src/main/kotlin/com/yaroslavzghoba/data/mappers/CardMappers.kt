package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.model.CardDbo
import com.yaroslavzghoba.model.Card

/**
 * Converts an instance of the [CardDbo] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun CardDbo.toCard() = Card(
    id = this.id,
    frontTitle = this.frontTitle,
    frontDescription = this.frontDescription,
    frontExample = this.frontExample,
    backTitle = this.frontTitle,
    backDescription = this.backDescription,
    backExample = this.backExample,
    nextTimeAt = this.nextTimeAt,
    currentIntervalMs = this.currentIntervalMs,
    collectionId = this.collectionId,
)

/**
 * Converts an instance of the [Card] class to an instance of the [CardDbo] class.
 */
@Suppress("unused")
fun Card.toCardDbo() = CardDbo(
    id = this.id,
    frontTitle = this.frontTitle,
    frontDescription = this.frontDescription,
    frontExample = this.frontExample,
    backTitle = this.frontTitle,
    backDescription = this.backDescription,
    backExample = this.backExample,
    nextTimeAt = this.nextTimeAt,
    currentIntervalMs = this.currentIntervalMs,
    collectionId = this.collectionId,
)