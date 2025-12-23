package space.zghoba.data.local.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Represents a database table object that stores active user sessions.
 */
object UserSessionsTable : IdTable<String>(name = "sessions") {

    override val id: Column<EntityID<String>> = varchar(name = "id", length = 64).entityId()
    val value = text(name = "value")

    override val primaryKey = PrimaryKey(id)
}