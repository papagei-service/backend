package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Example
import com.yaroslavzghoba.model.ExampleToInsertRequest
import com.yaroslavzghoba.model.ExampleToUpdateRequest

/**
 * Converts an instance of the [ExampleToInsertRequest] class to an instance of the [Example] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleToInsertRequest.toExample(id: Long?, cardId: Long) = Example(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = cardId,
)

/**
 * Converts an instance of the [ExampleToUpdateRequest] class to an instance of the [Example] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleToUpdateRequest.toExample(id: Long?) = Example(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = this.cardId,
)

/**
 * Converts an instance of the [Example] class to an instance of the [ExampleToInsertRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Example.toExampleToInsertRequest() = ExampleToInsertRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
)

/**
 * Converts an instance of the [Example] class to an instance of the [ExampleToUpdateRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Example.toExampleToUpdateRequest() = ExampleToUpdateRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = this.cardId
)