package com.yaroslavzghoba.model

/**
 * Represent a user of the service.
 *
 * @param username A unique identifier of the user's account.
 * @param password A special code word that used to log in the system.
 */
data class User(
    val username: String,
    val password: String,
)
