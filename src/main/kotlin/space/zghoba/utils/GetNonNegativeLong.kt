package space.zghoba.utils

import io.ktor.http.Parameters

/**
 * Get first value from the query parameters associated with a [name],
 * convert it to [Long], and returns that value if it is greater than or equal to 0.
 *
 * @param name Name of the query parameter that should be extracted.
 * @return The value of the corresponding query parameter if it greater than or equals to 0.
 *
 * @throws NoSuchElementException If the corresponding query parameter not found.
 * @throws NumberFormatException If the founded value cannot be converted to [Long].
 */
fun Parameters.getNonNegativeLong(name: String): Long {
    val string = this[name] ?: throw NoSuchElementException("Parameter \"$name\" not found.")
    val number = string.toLong()
    if (number >= 0) {
        return number
    } else {
        throw NumberFormatException("Parameter \"$name\" is not a non-negative integer.")
    }
}

/**
 * Get first value from the query parameters associated with a [name],
 * convert it to [Long], and returns that value if it is greater than or equal to 0,
 * or null if the name is not present.
 *
 * @param name Name of the query parameter that should be extracted.
 * @return The value of the corresponding query parameter if it greater than or equals to 0,
 * or null if it not found.
 *
 * @throws NoSuchElementException If the corresponding query parameter not found.
 */
fun Parameters.getNonNegativeLongOrNull(name: String): Long? {
    return try {
        this.getNonNegativeLong(name)
    } catch (_: NumberFormatException) {
        null
    }
}