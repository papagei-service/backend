package com.yaroslavzghoba.data.dao

import com.yaroslavzghoba.model.User

/**
 * Defines methods for storing and reading user accounts data.
 */
interface UserDao {

    /**
     * Searches for a user by its [username] in the user storage. If the user was found, the function returns it.
     * If the user is not found, the method returns `null`.
     *
     * @param username A unique user identifier that is used to search the user storage.
     * @return The user with the corresponding [username] if it was found and `null` if not.
     */
    suspend fun getByUsername(username: String): User?

    /**
     * Try to insert a user into the storage.
     *
     * @param user The user that must be inserted to the storage.
     * @throws IllegalArgumentException if the user with the same username is already exists in the storage.
     */
    suspend fun insert(user: User)

    /**
     * Try to update a user in the storage.
     *
     * @param user The user that must be updated to the storage.
     * @throws IllegalArgumentException If a user with the same username is not found.
     */
    suspend fun update(user: User)

    /**
     * Delete the user from the storage by its username.
     *
     * @param username The username of the user to be deleted.
     */
    suspend fun deleteByUsername(username: String)
}