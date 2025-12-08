package space.zghoba.data.mappers

import space.zghoba.data.local.dao.UserDao
import space.zghoba.model.User

/**
 * Converts an instance of the [UserDao] class to an instance of the [User] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun UserDao.toUser() = User(
    id = this.id.value,
    username = this.username,
    displayName = this.displayName,
    hashedPassword = this.hashedPassword,
    salt = this.salt,
)