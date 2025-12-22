package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import space.zghoba.data.local.tables.UserSessionsTable

class UserSessionDao(id: EntityID<String>) : Entity<String>(id = id) {
    companion object : EntityClass<String, UserSessionDao>(UserSessionsTable)

    var value by UserSessionsTable.value
}