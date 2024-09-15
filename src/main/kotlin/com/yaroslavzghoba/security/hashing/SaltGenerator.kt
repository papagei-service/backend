package com.yaroslavzghoba.security.hashing

/**
 * Defines methods for generating password salt.
 */
interface SaltGenerator {

    /**
     * Generates a salt for a password of a given length.
     *
     * @return A generated salt value.
     */
    fun generate(): String
}