package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import space.zghoba.data.local.tables.UsersTable

class UserDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<UserDao>(UsersTable)

    var username by UsersTable.username
    var displayName by UsersTable.displayName
    var hashedPassword by UsersTable.hashedPassword
    var salt by UsersTable.salt
}

