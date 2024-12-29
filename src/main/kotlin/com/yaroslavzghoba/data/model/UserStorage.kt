package com.yaroslavzghoba.data.model

import com.yaroslavzghoba.model.User

/**
 * Defines methods for storing and reading user accounts.
 */
interface UserStorage {

    /**
     * Searches for a user by its [id] in the storage. If the user was found, the function returns it.
     * If the user is not found, the method returns null.
     *
     * @param id A unique immutable user identifier that is used to search.
     * @return The user with the corresponding [id] if it was found and null if not.
     */
    suspend fun getById(id: Long): User?

    /**
     * Searches for a user by its [username] in the storage. If the user was found, the function returns it.
     * If the user is not found, the method returns null.
     *
     * @param username A unique mutable user identifier that is used to search.
     * @return The user with the corresponding [username] if it was found and null if not.
     */
    suspend fun getByUsername(username: String): User?

    /**
     * Try to insert a user into the storage.
     *
     * @param user The user that must be inserted to the storage.
     * @return Inserted user.
     *
     * @throws IllegalArgumentException if the user with the same id is already exists in the storage.
     */
    suspend fun insert(user: User): User

    /**
     * Try to update the user in the storage.
     *
     * @param user The user that must be updated.
     * @return Updated user.
     *
     * @throws IllegalArgumentException If a user with the same id is not found.
     */
    suspend fun update(user: User): User

    /**
     * Delete all users from the storage.
     */
    suspend fun deleteAll()

    /**
     * Delete the user from the storage by its id.
     *
     * @param id The unique identifier of the user to be deleted.
     */
    suspend fun deleteById(id: Long)
}