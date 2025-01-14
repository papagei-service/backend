package com.yaroslavzghoba.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Represents the answers to a single card in the process of repetition.
 */
enum class CardAnswer(val interval: Duration) {

    /**
     * A user does not remember an answer to the card
     */
    WRONG(interval = 1.minutes),

    /**
     * It was hard to remember an answer to the card.
     */
    HARD(interval = 10.minutes),

    /**
     * It was normal to remember an answer to the card.
     */
    NORMAL(interval = 30.minutes),

    /**
     * It was easy to remember an answer to the card.
     */
    EASY(interval = 90.minutes),
}