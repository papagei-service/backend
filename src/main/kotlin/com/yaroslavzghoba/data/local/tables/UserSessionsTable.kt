package com.yaroslavzghoba.data.local.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

/**
 * Represents a database table object that stores active user sessions.
 */
object UserSessionsTable : IdTable<String>(name = "sessions") {

    override val id: Column<EntityID<String>> = varchar(name = "id", length = 64).entityId()
    val value = text(name = "value")

    override val primaryKey = PrimaryKey(id)
}