package com.yaroslavzghoba.data

import com.yaroslavzghoba.model.User

/**Provides methods for working with the user repository*/
interface UserStorage {

    /**
     * Searches for a user by username in the user data storage. If the user was found,the function returns an instance
     * of the [User] class with the properties of this user. If the user is not found, the method returns `null`.
     *
     * @param username A unique user identifier that is used to search the user storage.
     * @return The user with the corresponding [username] if it was found and `null` if not.
     */
    fun getByUsername(username: String): User?

    /**
     * Try to insert a user into the data storage.
     *
     * @param user The user that must be inserted to the data storage.
     * @throws IllegalArgumentException if the user with the same username is already exists in the data storage.
     */
    fun insert(user: User)
}