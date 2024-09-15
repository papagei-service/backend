package com.yaroslavzghoba.security.hashing

import java.security.MessageDigest

/**
 * Provides an API for password hashing.
 *
 * @param pepper Random charactes added to the salted password during hashing.
 * @param algorithm An algorithm used for hashing.
 */
class HashingServiceImpl(
    private val pepper: String,
    private val algorithm: String,
) : HashingService {

    override fun hash(password: String, salt: String): String =
        hash(value = "$password$salt$pepper")

    private fun hash(value: String): String = MessageDigest
        .getInstance(algorithm)
        .digest(value.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}