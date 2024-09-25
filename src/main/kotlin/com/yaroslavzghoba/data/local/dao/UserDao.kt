package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.UsersTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDao(username: EntityID<String>) : Entity<String>(id = username) {
    companion object : EntityClass<String, UserDao>(UsersTable)

    var hashedPassword by UsersTable.hashedPassword
    var salt by UsersTable.salt
}

