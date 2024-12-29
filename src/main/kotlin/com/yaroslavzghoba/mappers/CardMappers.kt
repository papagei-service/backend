package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardRequest

/**
 * Converts an instance of the [CardRequest] class to an instance of the [Card] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CardRequest.toCard(collectionId: Long) = Card(
    id = this.id,
    nativeLanguageValue = this.nativeLanguageValue,
    nativeLanguageValueDescription = this.nativeLanguageValueDescription,
    nativeLanguageValueExample = this.nativeLanguageValueExample,
    foreignLanguageValue = this.foreignLanguageValue,
    foreignLanguageValueDescription = this.foreignLanguageValueDescription,
    foreignLanguageValueExample = this.foreignLanguageValueExample,
    nextTimeAt = this.nextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    collectionId = collectionId,
)

/**
 * Converts an instance of the [Card] class to an instance of the [CardRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Card.toCardRequest() = CardRequest(
    id = this.id,
    nativeLanguageValue = this.nativeLanguageValue,
    nativeLanguageValueDescription = this.nativeLanguageValueDescription,
    nativeLanguageValueExample = this.nativeLanguageValueExample,
    foreignLanguageValue = this.foreignLanguageValue,
    foreignLanguageValueDescription = this.foreignLanguageValueDescription,
    foreignLanguageValueExample = this.foreignLanguageValueExample,
    nextTimeAt = this.nextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
)