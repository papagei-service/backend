package com.yaroslavzghoba.data.local

import io.ktor.server.sessions.*
import io.ktor.util.collections.*

/**
 * Storage for storing and reading active user sessions.
 */
class UserSessionStorage : SessionStorage {

    private val sessions = ConcurrentMap<String, String>()

    override suspend fun write(id: String, value: String) {
        sessions[id] = value
    }

    override suspend fun invalidate(id: String) {
        sessions.remove(id)
    }

    override suspend fun read(id: String): String =
        sessions[id] ?: throw NoSuchElementException("Session $id not found")
}