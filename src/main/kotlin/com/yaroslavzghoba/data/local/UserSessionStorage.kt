package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.UserSessionDao
import com.yaroslavzghoba.data.local.tables.UserSessionsTable
import com.yaroslavzghoba.data.model.PurgeableSessionStorage
import com.yaroslavzghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere

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
            ?: throw NoSuchElementException("Session $id not found")
    }
}