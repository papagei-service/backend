package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.dao.UserDao
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

    override suspend fun getByOwnerUsername(ownerUsername: String): List<CardCollection> = suspendTransaction {
        CollectionDao
            .find { CollectionsTable.ownerUsername eq ownerUsername }
            .map { it.toCardCollection() }
    }

    override suspend fun insert(collection: CardCollection): CardCollection = suspendTransaction {
        // Get the owner of the collection
        val user = UserDao
            .find { UsersTable.id eq collection.ownerUsername }
            .firstOrNull()
            ?: throw NoSuchElementException("Collection owner not found in storage")

        CollectionDao.new(id = collection.id) {
            title = collection.title
            description = collection.description
            subjectType = collection.subjectType.name
            subjectLanguage = collection.subjectLanguage
            nativeLanguage = collection.nativeLanguage
            ownerUsername = user
        }.toCardCollection()
    }

    override suspend fun update(collection: CardCollection): CardCollection = suspendTransaction {
        if (collection.id == null)
            throw IllegalArgumentException("The identifier of the collection to be updated cannot be null")

        // Get the owner of the collection
        val user = UserDao
            .find { UsersTable.id eq collection.ownerUsername }
            .firstOrNull()
            ?: throw NoSuchElementException("Collection owner not found in storage")

        CollectionDao.findByIdAndUpdate(id = collection.id) {
            it.title = collection.title
            it.description = collection.description
            it.subjectType = collection.subjectType.name
            it.nativeLanguage = collection.nativeLanguage
            it.ownerUsername = user
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
}