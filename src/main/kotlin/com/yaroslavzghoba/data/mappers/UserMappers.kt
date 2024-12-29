package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.model.User

/**
 * Converts an instance of the [UserDao] class to an instance of the [User] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun UserDao.toUser() = User(
    id = this.id.value,
    username = this.username,
    hashedPassword = this.hashedPassword,
    salt = this.salt,
)