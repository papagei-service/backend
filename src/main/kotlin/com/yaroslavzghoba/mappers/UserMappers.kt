package com.yaroslavzghoba.mappers

import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.model.UserResponse

/**
 * Converts an instance of the [User] class to an instance of the [UserResponse] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun User.toUserResponse() = UserResponse(
    id = this.id,
    username = this.username,
)