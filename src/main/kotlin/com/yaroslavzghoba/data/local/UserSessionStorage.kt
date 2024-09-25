package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.UserSessionDao
import com.yaroslavzghoba.data.local.tables.UserSessionsTable
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of active user sessions in persistent memory.
 */
class UserSessionStorage : SessionStorage {

    override suspend fun write(id: String, value: String) {
        UserSessionDao.new(id = id) {
            this.value = value
        }
    }

    override suspend fun invalidate(id: String) {
        UserSessionsTable.deleteWhere {
            UserSessionsTable.id eq id
        }
    }

    override suspend fun read(id: String): String = UserSessionDao
        .find { UserSessionsTable.id eq id }
        .firstOrNull()?.value
        ?: throw NoSuchElementException("Session $id not found")
}