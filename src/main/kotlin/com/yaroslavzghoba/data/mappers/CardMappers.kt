package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.model.Card

/**
 * Converts an instance of the [CardDao] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun CardDao.toCard() = Card(
    id = this.id.value,
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