package com.yaroslavzghoba.utils

import kotlin.random.Random

/**
 * Provides methods for generating random strings that can be used as keys.
 */
class KeyGeneratorImpl : KeyGenerator {

    override fun generate(length: Int): String =
        CharArray(length) { Random.nextChar() }.joinToString(separator = "")
}