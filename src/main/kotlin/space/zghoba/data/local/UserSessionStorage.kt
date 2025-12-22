package space.zghoba.data.local

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import space.zghoba.data.local.dao.UserSessionDao
import space.zghoba.data.local.tables.UserSessionsTable
import space.zghoba.data.model.PurgeableSessionStorage

/**
 * Represents a storage of active user sessions in persistent memory.
 */
class UserSessionStorage : PurgeableSessionStorage {

    override suspend fun write(id: String, value: String): Unit = suspendTransaction {
        UserSessionDao.new(id = id) {
            this.value = value
        }
    }

    override suspend fun invalidateAll(): Unit = suspendTransaction {
        UserSessionsTable.deleteAll()
    }

    override suspend fun invalidate(id: String): Unit = suspendTransaction {
        UserSessionsTable.deleteWhere {
            UserSessionsTable.id eq id
        }
    }

    override suspend fun read(id: String): String = suspendTransaction {
        UserSessionDao
            .find { UserSessionsTable.id eq id }
            .firstOrNull()?.value
            ?: throw NoSuchElementException("Session with id equals to $id not found.")
    }
}