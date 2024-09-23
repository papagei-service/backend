package com.yaroslavzghoba.data.fake

import com.yaroslavzghoba.data.dao.CollectionDao
import com.yaroslavzghoba.model.CardCollection

class FakeCollectionDao : CollectionDao {

    private val collections: MutableList<CardCollection> = mutableListOf()

    override suspend fun getById(id: Long): CardCollection? {
        return collections.find { it.id == id }
    }

    override suspend fun getByOwnerUsername(ownerUsername: String): List<CardCollection> {
        return collections.filter { it.ownerUsername == ownerUsername }
    }

    override suspend fun insert(collection: CardCollection) {
        val isExists = collections.any { it.id == collection.id }
        require(!isExists) { "A collection with the same id is already exists" }
        collections.add(collection)
    }

    override suspend fun update(collection: CardCollection) {
        val correspondingUser = collections.firstOrNull { it.id == collection.id }
        requireNotNull(correspondingUser) { "A collection with the same username is not found" }
        val index = collections.indexOf(correspondingUser)
        collections.removeAt(index = index)
        collections.add(index = index, element = collection)
    }

    override suspend fun deleteById(id: Long) {
        val correspondingUser = collections.firstOrNull { it.id == id }
        correspondingUser?.let { user ->
            collections.remove(user)
        }
    }
}