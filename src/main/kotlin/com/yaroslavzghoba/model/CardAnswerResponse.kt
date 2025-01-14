package com.yaroslavzghoba.model

/**
 * Represents an answer to a single card that sent by a client.
 *
 * @param answer A name of the one of [CardAnswer].
 */
data class CardAnswerResponse(
    val answer: String,
)