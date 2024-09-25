package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.data.local.tables.UsersTable
import com.yaroslavzghoba.data.mappers.toUser
import com.yaroslavzghoba.data.model.UserStorage
import com.yaroslavzghoba.model.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of user accounts in persistent memory.
 */
class UserStorageImpl : UserStorage {

    override suspend fun getByUsername(username: String): User? = UserDao
        .find { UsersTable.id eq username }
        .map { it.toUser() }
        .firstOrNull()

    override suspend fun insert(user: User) {
        UserDao.new(id = user.username) {
            hashedPassword = user.hashedPassword
            salt = user.salt
        }
    }

    override suspend fun update(user: User) {
        UserDao.findByIdAndUpdate(id = user.username) {
            it.hashedPassword = user.hashedPassword
            it.salt = user.salt
        }
    }

    override suspend fun deleteByUsername(username: String) {
        UsersTable.deleteWhere { id eq username }
    }
}