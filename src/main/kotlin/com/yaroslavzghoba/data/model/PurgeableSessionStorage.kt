package com.yaroslavzghoba.data.model

import io.ktor.server.sessions.*

/**
 * A storage that implements the minimum required methods for reading, writing, and deleting individual sessions,
 * as well as a method for completely deleting all sessions.
 */
interface PurgeableSessionStorage : SessionStorage {

    /**
     * Invalidates all sessions.
     * This method prevents access to any session after this call.
     */
    suspend fun invalidateAll()
}