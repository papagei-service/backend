package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.tables.CollectionsTable
import com.yaroslavzghoba.data.mappers.toCardCollection
import com.yaroslavzghoba.data.model.CollectionStorage
import com.yaroslavzghoba.model.CardCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of card collections in persistent memory.
 *
 * @throws IllegalArgumentException When trying to update a collection with a `null` identifier.
 */
class CollectionStorageImpl : CollectionStorage {

    override suspend fun getById(id: Long): CardCollection? = CollectionDao
        .find { CollectionsTable.id eq id }
        .map { it.toCardCollection() }
        .firstOrNull()

    override suspend fun getByOwnerUsername(ownerUsername: String): List<CardCollection> = CollectionDao
        .find { CollectionsTable.ownerUsername eq ownerUsername }
        .map { it.toCardCollection() }

    override suspend fun insert(collection: CardCollection) {
        CollectionDao.new(id = collection.id) {
            title = collection.title
            description = collection.description
            subjectType = collection.subjectType.name
            subjectLanguage = collection.subjectLanguage
            nativeLanguage = collection.nativeLanguage
            ownerUsername = collection.ownerUsername
        }
    }

    override suspend fun update(collection: CardCollection) {
        if (collection.id == null)
            throw IllegalArgumentException("The card collection with index `null` cannot be updated")
        CollectionDao.findByIdAndUpdate(id = collection.id) {
            it.title = collection.title
            it.description = collection.description
            it.subjectType = collection.subjectType.name
            it.nativeLanguage = collection.nativeLanguage
            it.ownerUsername = collection.ownerUsername
        }
    }

    override suspend fun deleteById(id: Long) {
        CollectionsTable.deleteWhere {
            CollectionsTable.id eq id
        }
    }
}