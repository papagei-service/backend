package space.zghoba.data.local.dao

import space.zghoba.data.local.tables.UserSessionsTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserSessionDao(id: EntityID<String>) : Entity<String>(id = id) {
    companion object : EntityClass<String, UserSessionDao>(UserSessionsTable)

    var value by UserSessionsTable.value
}