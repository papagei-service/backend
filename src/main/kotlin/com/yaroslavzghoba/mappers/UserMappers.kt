package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.model.Account

/**
 * Converts an instance of the [User] class to an instance of the [Account] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun User.toUserResponse() = Account(
    id = this.id,
    displayName = this.displayName,
    username = this.username,
)