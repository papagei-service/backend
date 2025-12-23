package space.zghoba.data.local.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * Represents a database table object that stores user accounts.
 */
object UsersTable : LongIdTable(name = "users", columnName = "id") {

    val username = varchar(name = "username", length = 64).uniqueIndex()
    val hashedPassword = varchar(name = "hashed_password", length = 128)
    val displayName = varchar(name = "display_name", length = 48)
    val salt = varchar(name = "salt", length = 64)
}