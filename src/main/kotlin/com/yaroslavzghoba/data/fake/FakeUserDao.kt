package com.yaroslavzghoba.data.fake

import com.yaroslavzghoba.data.dao.UserDao
import com.yaroslavzghoba.model.User

class FakeUserDao : UserDao {

    private val users: MutableList<User> = mutableListOf()

    override suspend fun getByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    override suspend fun insert(user: User) {
        val isExists = users.any { it.username == user.username }
        require(!isExists) { "The username is already taken" }
        users.add(user)
    }

    override suspend fun update(user: User) {
        val correspondingUser = users.firstOrNull { it.username == user.username }
        requireNotNull(correspondingUser) { "A user with the same username is not found" }
        val index = users.indexOf(correspondingUser)
        users.removeAt(index = index)
        users.add(index = index, element = user)
    }

    override suspend fun deleteByUsername(username: String) {
        val correspondingUser = users.firstOrNull { it.username == username }
        correspondingUser?.let { user ->
            users.remove(user)
        }
    }
}