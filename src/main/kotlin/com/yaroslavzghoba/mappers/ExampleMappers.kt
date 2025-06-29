package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Example
import com.yaroslavzghoba.model.ExampleInsertRequest
import com.yaroslavzghoba.model.ExampleUpdateRequest

/**
 * Converts an instance of the [ExampleInsertRequest] class to an instance of the [Example] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleInsertRequest.toExample(id: Long?, cardId: Long) = Example(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = cardId,
)

/**
 * Converts an instance of the [ExampleUpdateRequest] class to an instance of the [Example] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleUpdateRequest.toExample(id: Long?) = Example(
    id = id,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = this.cardId,
)

/**
 * Converts an instance of the [Example] class to an instance of the [ExampleInsertRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Example.toExampleInsertRequest() = ExampleInsertRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
)

/**
 * Converts an instance of the [Example] class to an instance of the [ExampleUpdateRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun Example.toExampleUpdateRequest() = ExampleUpdateRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = this.cardId
)

/**
 * Converts an instance of the [ExampleInsertRequest] class to an instance of the [ExampleUpdateRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleInsertRequest.toExampleUpdateRequest(cardId: Long) = ExampleUpdateRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = cardId,
)

/**
 * Converts an instance of the [ExampleUpdateRequest] class to an instance of the [ExampleInsertRequest] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleUpdateRequest.toExampleInsertRequest() = ExampleInsertRequest(
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
)