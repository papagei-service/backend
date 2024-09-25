package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.model.User

/**
 * Converts an instance of the [UserDao] class to an instance of the [User] class.
 */
@Suppress("unused")
fun UserDao.toUser() = User(
    username = this.id.value,
    hashedPassword = this.hashedPassword,
    salt = this.salt,
)