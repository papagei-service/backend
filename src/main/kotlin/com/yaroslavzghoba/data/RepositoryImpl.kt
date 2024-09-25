package com.yaroslavzghoba.data

import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.data.model.CollectionStorage
import com.yaroslavzghoba.data.model.UserStorage
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.model.User

/**
 * Represents a storage of user data on the local disk.
 */
class RepositoryImpl(
    private val userStorage: UserStorage,
    private val collectionStorage: CollectionStorage,
    private val cardStorage: CardStorage,
) : Repository {

    override suspend fun getUserByUsername(username: String): User? {
        return userStorage.getByUsername(username = username)
    }

    override suspend fun insertUser(user: User) {
        userStorage.insert(user = user)
    }

    override suspend fun updateUser(user: User) {
        userStorage.update(user = user)
    }

    override suspend fun deleteUserByUsername(username: String) {
        userStorage.deleteByUsername(username = username)
    }

    override suspend fun getCollectionById(id: Long): CardCollection? {
        return collectionStorage.getById(id = id)
    }

    override suspend fun getCollectionsByOwnerUsername(ownerUsername: String): List<CardCollection> {
        return collectionStorage.getByOwnerUsername(ownerUsername = ownerUsername)
    }

    override suspend fun insertCollection(collection: CardCollection) {
        collectionStorage.insert(collection = collection)
    }

    override suspend fun updateCollection(collection: CardCollection) {
        collectionStorage.update(collection = collection)
    }

    override suspend fun deleteCollectionById(id: Long) {
        collectionStorage.deleteById(id = id)
    }

    override suspend fun getCardById(id: Long): Card? {
        return cardStorage.getById(id = id)
    }

    override suspend fun getCardsByCollectionId(id: Long): List<Card> {
        return cardStorage.getByCollectionId(id = id)
    }

    override suspend fun insertCard(card: Card) {
        return cardStorage.insert(card = card)
    }

    override suspend fun updateCard(card: Card) {
        cardStorage.update(card = card)
    }

    override suspend fun deleteCardById(id: Long) {
        cardStorage.deleteById(id = id)
    }
}