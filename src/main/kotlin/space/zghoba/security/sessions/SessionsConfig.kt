package space.zghoba.security.sessions

import space.zghoba.data.model.PurgeableSessionStorage

/**
 * Represents the configuration of user sessions.
 *
 * @param sessionStorage Storage where active user sessions are stored.
 * @param lifetimeMs Lifetime of a single session in milliseconds.
 */
data class SessionsConfig(
    val sessionStorage: PurgeableSessionStorage,
    val lifetimeMs: Long?,
)