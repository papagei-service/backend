package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.Account
import com.yaroslavzghoba.model.User

/**
 * Converts an instance of the [User] class to an instance of the [Account] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun User.toAccount() = Account(
    id = this.id,
    displayName = this.displayName,
    username = this.username,
)