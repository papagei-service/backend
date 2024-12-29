package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.UsersTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<UserDao>(UsersTable)

    var username by UsersTable.username
    var hashedPassword by UsersTable.hashedPassword
    var salt by UsersTable.salt
}

