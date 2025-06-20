package com.yaroslavzghoba.data

import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.data.model.CollectionStorage
import com.yaroslavzghoba.data.model.ExampleStorage
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
    private val exampleStorage: ExampleStorage,
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

    override suspend fun getCollectionsByCardId(
        id: Long,
        limit: Int,
        offset: Long
    ): List<CardCollection> = collectionStorage.getByCardId(
        id = id,
        limit = limit,
        offset = offset,
    )

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

    override suspend fun deleteCollectionsByOwnerId(id: Long) {
        collectionStorage.deleteByOwnerId(id = id)
    }

    override suspend fun getCardById(id: Long): Card? {
        return cardStorage.getById(id = id)
    }

    override suspend fun getCardsByOwnerId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): List<Card> = cardStorage.getByOwnerId(
        id = id,
        sortByFirstPriority = sortByFirstPriority,
        sortBySecondPriority = sortBySecondPriority,
        nextTimeBefore = nextTimeBefore,
        limit = limit,
        offset = offset,
    )

    override suspend fun getCardsByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long
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

    override suspend fun deleteCardsByOwnerId(id: Long) {
        cardStorage.deleteByOwnerId(id = id)
    }

    override suspend fun getExampleById(id: Long): Example? {
        return exampleStorage.getById(id = id)
    }

    override suspend fun getExamplesByCardId(
        id: Long,
        limit: Int,
        offset: Long,
    ): List<Example> = exampleStorage.getByCardId(
        id = id,
        limit = limit,
        offset = offset,
    )

    override suspend fun insertExample(example: Example): Example {
        return exampleStorage.insert(example = example)
    }

    override suspend fun updateExample(example: Example): Example {
        return exampleStorage.update(example = example)
    }

    override suspend fun deleteAllExamples() {
        exampleStorage.deleteAll()
    }

    override suspend fun deleteExampleById(id: Long) {
        exampleStorage.deleteById(id = id)
    }

    override suspend fun deleteExampleByCardId(id: Long) {
        exampleStorage.deleteByCardId(id = id)
    }

    override suspend fun clear() {
        // It is important to delete from examples to users
        // Since the user cannot be deleted while collections/cards refer to him,
        // and cards while examples refer to them
        this.deleteAllExamples()
        this.deleteAllCards()
        this.deleteAllCollections()
        this.deleteAllUsers()
    }
}