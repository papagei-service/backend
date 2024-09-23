package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.model.UserDbo
import com.yaroslavzghoba.model.User

/**
 * Converts an instance of the [UserDbo] class to an instance of the [User] class.
 */
@Suppress("unused")
fun UserDbo.toUser() = User(
    username = this.username,
    hashedPassword = this.hashedPassword,
    salt = this.salt,
)

/**
 * Converts an instance of the [User] class to an instance of the [UserDbo] class.
 */
@Suppress("unused")
fun User.toUserDbo() = UserDbo(
    username = this.username,
    hashedPassword = this.hashedPassword,
    salt = this.salt,
)