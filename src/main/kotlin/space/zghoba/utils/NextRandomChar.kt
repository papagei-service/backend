package space.zghoba.utils

import kotlin.random.Random

/**
 * Randomly returns a random [Char].
 *
 * @param includeDigits Indicates whether the function can return digits.
 * @return Randomly selected character.
 */
@Suppress("nothing_to_inline", "UnusedReceiverParameter")
inline fun Random.nextChar(includeDigits: Boolean = true): Char {
    return ('A'..'Z').plus('a'..'z')
        .apply { if (includeDigits) plus(('0'..'9')) }
        .random()
}