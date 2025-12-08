package space.zghoba.data.local

import space.zghoba.data.local.dao.CollectionDao
import space.zghoba.data.local.dao.UserDao
import space.zghoba.data.local.tables.CollectionsCardsTable
import space.zghoba.data.local.tables.CollectionsTable
import space.zghoba.data.local.tables.UsersTable
import space.zghoba.data.mappers.toCardCollection
import space.zghoba.data.model.CollectionStorage
import space.zghoba.model.CardCollection
import space.zghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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

    override suspend fun getByOwnerId(
        id: Long,
        limit: Int,
        offset: Long
    ): Pair<Long, List<CardCollection>> = suspendTransaction {
        val condition: SqlExpressionBuilder.() -> Op<Boolean> = {
            CollectionsTable.ownerId eq id
        }

        // Apply filters to collections, create pairs of collections using limit and offset,
        // and the total number of cards found without applying limits and offsets.
        val totalCountColumn = CollectionsTable.id.count().over().alias("total_count")
        val collectionsWithTotalCount: List<Pair<CardCollection, Long>> = CollectionsTable
            .select(CollectionsTable.columns + totalCountColumn)
            .where(predicate = condition)
            .limit(limit).offset(offset)
            .map { row ->
                CardCollection(
                    id = row[CollectionsTable.id].value,
                    title = row[CollectionsTable.title],
                    description = row[CollectionsTable.description],
                    knownLanguage = row[CollectionsTable.knownLanguage],
                    learningLanguage = row[CollectionsTable.learningLanguage],
                    ownerId = row[CollectionsTable.ownerId].value,
                ) to row[totalCountColumn]
            }

        // Convert `List<Pair<CardCollection, Long>>` to `Pair<Long, List<CardCollection>>`
        // If the list of pairs is empty, then make another request to get the total number of collections
        // and return it with an empty list of collections.
        val result: Pair<Long, List<CardCollection>> = if (collectionsWithTotalCount.isEmpty()) {
            val totalCount = CollectionsTable
                .selectAll()
                .where(predicate = condition)
                .count()
            totalCount to emptyList()
        } else {
            val collections = collectionsWithTotalCount.map { it.first }
            val totalCount = collectionsWithTotalCount.first().second
            totalCount to collections
        }

        result
    }

    override suspend fun getByCardId(
        id: Long,
        limit: Int,
        offset: Long
    ): Pair<Long, List<CardCollection>> = suspendTransaction {
        val condition: SqlExpressionBuilder.() -> Op<Boolean> = {
            CollectionsCardsTable.cardId eq id
        }

        // Apply filters to collections, create pairs of collections using limit and offset,
        // and the total number of cards found without applying limits and offsets.
        val totalCountColumn = CollectionsTable.id.count().over().alias("total_count")
        val collectionsWithTotalCount: List<Pair<CardCollection, Long>> = CollectionsTable
            .innerJoin(CollectionsCardsTable)
            .select(CollectionsTable.columns + totalCountColumn)
            .where(predicate = condition)
            .withDistinct()
            .limit(limit).offset(offset)
            .map { row ->
                CardCollection(
                    id = row[CollectionsTable.id].value,
                    title = row[CollectionsTable.title],
                    description = row[CollectionsTable.description],
                    knownLanguage = row[CollectionsTable.knownLanguage],
                    learningLanguage = row[CollectionsTable.learningLanguage],
                    ownerId = row[CollectionsTable.ownerId].value,
                ) to row[totalCountColumn]
            }

        // Convert `List<Pair<CardCollection, Long>>` to `Pair<Long, List<CardCollection>>`
        // If the list of pairs is empty, then make another request to get the total number of collections
        // and return it with an empty list of collections.
        val result: Pair<Long, List<CardCollection>> = if (collectionsWithTotalCount.isEmpty()) {
            val totalCount = CollectionsTable
                .innerJoin(CollectionsCardsTable)
                .selectAll()
                .where(predicate = condition)
                .count()
            totalCount to emptyList()
        } else {
            val collections = collectionsWithTotalCount.map { it.first }
            val totalCount = collectionsWithTotalCount.first().second
            totalCount to collections
        }

        result
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
            it.knownLanguage = collection.knownLanguage
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

    override suspend fun deleteByOwnerId(id: Long): Unit = suspendTransaction {
        CollectionsTable.deleteWhere {
            CollectionsTable.ownerId eq id
        }
    }
}