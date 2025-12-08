package space.zghoba.mappers

import space.zghoba.model.Account
import space.zghoba.model.User

/**
 * Converts an instance of the [User] class to an instance of the [Account] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun User.toAccount() = Account(
    id = this.id,
    displayName = this.displayName,
    username = this.username,
)