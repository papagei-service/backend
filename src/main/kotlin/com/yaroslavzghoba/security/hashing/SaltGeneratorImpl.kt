package com.yaroslavzghoba.security.hashing

import com.yaroslavzghoba.utils.nextChar
import kotlin.random.Random

/**
 * Provides methods for generating password salt.
 *
 * @param length A length of the generated salt.
 */
class SaltGeneratorImpl(private val length: Int) : SaltGenerator {

    override fun generate(): String =
        CharArray(length) { Random.nextChar() }.joinToString(separator = "")
}