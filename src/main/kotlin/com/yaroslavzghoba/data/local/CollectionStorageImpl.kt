package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.data.local.tables.CollectionsCardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsTable
import com.yaroslavzghoba.data.local.tables.UsersTable
import com.yaroslavzghoba.data.mappers.toCardCollection
import com.yaroslavzghoba.data.model.CollectionStorage
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of card collections in persistent memory.
 *
 * @throws IllegalArgumentException When trying to update a collection with a `null` identifier.
 */
class CollectionStorageImpl : CollectionStorage {

    override suspend fun getById(id: Long): CardCollection? = suspendTransaction {
        CollectionDao
            .find { CollectionsTable.id eq id }
            .map { it.toCardCollection() }
            .firstOrNull()
    }

    override suspend fun getByOwnerId(ownerId: Long): List<CardCollection> = suspendTransaction {
        CollectionDao
            .find { CollectionsTable.ownerId eq ownerId }
            .map { it.toCardCollection() }
    }

    override suspend fun getByCardId(
        id: Long,
        limit: Int,
        offset: Long
    ): List<CardCollection> = suspendTransaction {
        val query = CollectionsTable.innerJoin(CollectionsCardsTable)
            .select(CollectionsTable.columns)
            .where {
                CollectionsCardsTable.cardId eq id
            }
            .withDistinct()

        CollectionDao.wrapRows(query)
            .limit(limit).offset(offset)
            .toList().map { it.toCardCollection() }
    }

    override suspend fun insert(collection: CardCollection): CardCollection = suspendTransaction {
        // Get the owner of the collection
        val user = UserDao
            .find { UsersTable.id eq collection.ownerId }
            .firstOrNull()
            ?: throw NoSuchElementException("Collection owner not found in storage")

        CollectionDao.new(id = collection.id) {
            title = collection.title
            description = collection.description
            knownLanguage = collection.knownLanguage
            learningLanguage = collection.learningLanguage
            ownerId = user
        }.toCardCollection()
    }

    override suspend fun update(collection: CardCollection): CardCollection = suspendTransaction {
        if (collection.id == null)
            throw IllegalArgumentException("The id of the collection to be updated cannot be null")

        // Get the owner of the collection
        val user = UserDao
            .find { UsersTable.id eq collection.ownerId }
            .firstOrNull()
            ?: throw NoSuchElementException("Collection owner not found in storage")

        CollectionDao.findByIdAndUpdate(id = collection.id) {
            it.title = collection.title
            it.description = collection.description
            it.knownLanguage = collection.learningLanguage
            it.learningLanguage = collection.learningLanguage
            it.ownerId = user
        }?.toCardCollection()
            ?: throw NoSuchElementException("Corresponding collection not found in storage")
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        CollectionsTable.deleteAll()
    }

    override suspend fun deleteById(id: Long): Unit = suspendTransaction {
        CollectionsTable.deleteWhere {
            CollectionsTable.id eq id
        }
    }

    override suspend fun deleteByOwnerId(id: Long) {
        CollectionsTable.deleteWhere {
            CollectionsTable.ownerId eq id
        }
    }
}