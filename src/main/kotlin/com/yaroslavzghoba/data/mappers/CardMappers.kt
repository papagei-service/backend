package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.model.Card

/**
 * Converts an instance of the [CardDao] class to an instance of the [Card] class.
 */
@Suppress("unused")
fun CardDao.toCard() = Card(
    id = this.id.value,
    nativeLanguageValue = this.nativeLanguageValue,
    nativeLanguageValueDescription = this.nativeLanguageValueDescription,
    nativeLanguageValueExample = this.nativeLanguageValueExample,
    foreignLanguageValue = this.foreignLanguageValue,
    foreignLanguageValueDescription = this.foreignLanguageValueDescription,
    foreignLanguageValueExample = this.foreignLanguageValueExample,
    nextTimeAt = this.nextTimeAt,
    correctAnswersInRow = this.correctAnswersInRow,
    collectionId = this.collectionId.id.value,
)