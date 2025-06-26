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

    override suspend fun getById(id: Long): User? = suspendTransaction {
        UserDao
            .find { UsersTable.id eq id }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDao
            .find { UsersTable.username eq username }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun insert(user: User): User = suspendTransaction {
        UserDao.new(id = user.id) {
            username = user.username
            displayName = user.displayName
            hashedPassword = user.hashedPassword
            salt = user.salt
        }.toUser()
    }

    override suspend fun update(user: User): User = suspendTransaction {
        if (user.id == null)
            throw IllegalArgumentException("The identifier of the user to be updated cannot be null")

        UserDao.findByIdAndUpdate(id = user.id) {
            it.username = user.username
            it.displayName = user.displayName
            it.hashedPassword = user.hashedPassword
            it.salt = user.salt
        }?.toUser()
            ?: throw NoSuchElementException("Corresponding user not found")
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        UsersTable.deleteAll()
    }

    override suspend fun deleteById(id: Long): Unit = suspendTransaction {
        UsersTable.deleteWhere { UsersTable.id eq id }
    }
}