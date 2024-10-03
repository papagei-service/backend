package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.data.local.tables.UsersTable
import com.yaroslavzghoba.data.mappers.toUser
import com.yaroslavzghoba.data.model.UserStorage
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of user accounts in persistent memory.
 */
class UserStorageImpl : UserStorage {

    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDao
            .find { UsersTable.id eq username }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun insert(user: User): User = suspendTransaction {
        UserDao.new(id = user.username) {
            hashedPassword = user.hashedPassword
            salt = user.salt
        }.toUser()
    }

    override suspend fun update(user: User): User = suspendTransaction {
        UserDao.findByIdAndUpdate(id = user.username) {
            it.hashedPassword = user.hashedPassword
            it.salt = user.salt
        }?.toUser()
            ?: throw NoSuchElementException("Corresponding user not found")
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        UsersTable.deleteAll()
    }

    override suspend fun deleteByUsername(username: String): Unit = suspendTransaction {
        UsersTable.deleteWhere { id eq username }
    }
}