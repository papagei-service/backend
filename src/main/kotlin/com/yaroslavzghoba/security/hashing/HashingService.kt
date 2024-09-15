package com.yaroslavzghoba.security.hashing

/**
 * Defines an API for password hashing.
 */
interface HashingService {

    /**
     * Hash a password.
     *
     * @param password A password that must be hashed.
     * @param salt Random characters added to the user's password during hashing.
     *
     * @return A hashed combination of the user's password and salt.
     */
    fun hash(password: String, salt: String): String
}