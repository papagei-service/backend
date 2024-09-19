package com.yaroslavzghoba.security.hashing

/**
 * This is a password salt configuration that is added when the password is hashed
 * and stored along with the hash in plain text.
 *
 * @param minLength Minimum length of password salt.
 * @param maxLength Maximum length of password salt.
 */
data class PasswordSaltConfig(
    val minLength: Int,
    val maxLength: Int,
)
