package space.zghoba.data.local

import space.zghoba.data.local.dao.UserDao
import space.zghoba.data.local.tables.UsersTable
import space.zghoba.data.mappers.toUser
import space.zghoba.data.model.UserStorage
import space.zghoba.model.User
import space.zghoba.utils.suspendTransaction
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