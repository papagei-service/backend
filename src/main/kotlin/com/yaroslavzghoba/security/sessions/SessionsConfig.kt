package com.yaroslavzghoba.security.sessions

import io.ktor.server.sessions.*

/**
 * Represents the configuration of user sessions.
 *
 * @param sessionStorage Storage where active user sessions are stored.
 * @param lifetimeMs Lifetime of a single session in milliseconds.
 */
data class SessionsConfig(
    val sessionStorage: SessionStorage,
    val lifetimeMs: Long?,
)