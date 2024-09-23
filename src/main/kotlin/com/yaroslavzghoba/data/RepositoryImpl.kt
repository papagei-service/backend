package com.yaroslavzghoba.data

import com.yaroslavzghoba.data.dao.CardDao
import com.yaroslavzghoba.data.dao.CollectionDao
import com.yaroslavzghoba.data.dao.UserDao
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.model.User

/**
 * Implements methods for storing and reading user data.
 */
class RepositoryImpl(
    private val userDao: UserDao,
    private val collectionDao: CollectionDao,
    private val cardDao: CardDao,
) : Repository {

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getByUsername(username = username)
    }

    override suspend fun insertUser(user: User) {
        userDao.insert(user = user)
    }

    override suspend fun updateUser(user: User) {
        userDao.update(user = user)
    }

    override suspend fun deleteUserByUsername(username: String) {
        userDao.deleteByUsername(username = username)
    }

    override suspend fun getCollectionById(id: Long): CardCollection? {
        return collectionDao.getById(id = id)
    }

    override suspend fun getCollectionsByOwnerUsername(ownerUsername: String): List<CardCollection> {
        return collectionDao.getByOwnerUsername(ownerUsername = ownerUsername)
    }

    override suspend fun insertCollection(collection: CardCollection) {
        collectionDao.insert(collection = collection)
    }

    override suspend fun updateCollection(collection: CardCollection) {
        collectionDao.update(collection = collection)
    }

    override suspend fun deleteCollectionById(id: Long) {
        collectionDao.deleteById(id = id)
    }

    override suspend fun getCardById(id: Long): Card? {
        return cardDao.getById(id = id)
    }

    override suspend fun getCardsByCollectionId(id: Long): List<Card> {
        return cardDao.getByCollectionId(id = id)
    }

    override suspend fun insertCard(card: Card) {
        return cardDao.insert(card = card)
    }

    override suspend fun updateCard(card: Card) {
        cardDao.update(card = card)
    }

    override suspend fun deleteCardById(id: Long) {
        cardDao.deleteById(id = id)
    }
}