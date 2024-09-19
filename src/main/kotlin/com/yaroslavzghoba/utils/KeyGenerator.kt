package com.yaroslavzghoba.utils

/**
 * Defines methods for generating random strings that can be used as keys.
 */
interface KeyGenerator {

    /**
     * Generates a random string of a given length.
     *
     * @param length Length of the generated key.
     * @return A generated string.
     */
    fun generate(length: Int): String
}