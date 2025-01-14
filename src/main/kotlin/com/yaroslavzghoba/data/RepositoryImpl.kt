package com.yaroslavzghoba.data

import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.data.model.CollectionStorage
import com.yaroslavzghoba.data.model.UserStorage
import com.yaroslavzghoba.model.*
import kotlinx.datetime.Instant

/**
 * Represents a storage of user data on the local disk.
 */
class RepositoryImpl(
    private val userStorage: UserStorage,
    private val collectionStorage: CollectionStorage,
    private val cardStorage: CardStorage,
) : Repository {

    override suspend fun getUserById(id: Long): User? {
        return userStorage.getById(id = id)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userStorage.getByUsername(username = username)
    }

    override suspend fun insertUser(user: User): User {
        return userStorage.insert(user = user)
    }

    override suspend fun updateUser(user: User): User {
        return userStorage.update(user = user)
    }

    override suspend fun deleteAllUsers() {
        userStorage.deleteAll()
    }

    override suspend fun deleteUserById(id: Long) {
        userStorage.deleteById(id = id)
    }

    override suspend fun getCollectionById(id: Long): CardCollection? {
        return collectionStorage.getById(id = id)
    }

    override suspend fun getCollectionsByOwnerId(ownerId: Long): List<CardCollection> {
        return collectionStorage.getByOwnerId(ownerId = ownerId)
    }

    override suspend fun insertCollection(collection: CardCollection): CardCollection {
        return collectionStorage.insert(collection = collection)
    }

    override suspend fun updateCollection(collection: CardCollection): CardCollection {
        return collectionStorage.update(collection = collection)
    }

    override suspend fun deleteAllCollections() {
        collectionStorage.deleteAll()
    }

    override suspend fun deleteCollectionById(id: Long) {
        collectionStorage.deleteById(id = id)
    }

    override suspend fun getCardById(id: Long): Card? {
        return cardStorage.getById(id = id)
    }

    override suspend fun getCardsByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): List<Card> = cardStorage.getByCollectionId(
        id = id,
        sortByFirstPriority = sortByFirstPriority,
        sortBySecondPriority = sortBySecondPriority,
        nextTimeBefore = nextTimeBefore,
        limit = limit,
        offset = offset,
    )

    override suspend fun insertCard(card: Card): Card {
        return cardStorage.insert(card = card)
    }

    override suspend fun updateCard(card: Card): Card {
        return cardStorage.update(card = card)
    }

    override suspend fun deleteAllCards() {
        cardStorage.deleteAll()
    }

    override suspend fun deleteCardById(id: Long) {
        cardStorage.deleteById(id = id)
    }

    override suspend fun clear() {
        // It is important to delete from cards to users
        // Since the user cannot be deleted while collections refer to him, and those while cards refer to them
        this.deleteAllCards()
        this.deleteAllCollections()
        this.deleteAllUsers()
    }
}