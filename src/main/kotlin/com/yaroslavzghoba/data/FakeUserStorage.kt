package com.yaroslavzghoba.data

import com.yaroslavzghoba.model.User

class FakeUserStorage : UserStorage {

    private val users: MutableList<User> = mutableListOf()

    override fun getByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    override fun insert(user: User) {
        val isExists = users.any { it.username == user.username }
        require(!isExists) { "The username is already taken" }
        users.add(user)
    }
}