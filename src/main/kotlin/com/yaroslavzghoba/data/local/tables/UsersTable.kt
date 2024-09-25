package com.yaroslavzghoba.data.local.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

/**
 * Represents a database table object that stores user accounts.
 */
object UsersTable : IdTable<String>(name = "users") {

    // It is actually username
    override val id: Column<EntityID<String>>
        get() = varchar(name = "username", length = 64).entityId()

    val hashedPassword = varchar("hashed_password", 64)
    val salt = varchar(name = "salt", length = 64)
}