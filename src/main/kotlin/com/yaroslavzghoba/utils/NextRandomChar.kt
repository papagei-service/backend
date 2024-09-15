package com.yaroslavzghoba.utils

import kotlin.random.Random

/**
 * Randomly returns a random [Char].
 *
 * @param includeDigits Indicates whether the function can return digits.
 * @return Randomly selected characted.
 */
fun Random.nextChar(includeDigits: Boolean = true): Char {
    return ('A'..'Z').plus('a'..'z')
        .apply { if (includeDigits) plus(('0'..'9')) }
        .random()
}