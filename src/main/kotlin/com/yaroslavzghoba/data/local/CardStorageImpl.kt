package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.dao.CollectionCardDao
import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.data.local.tables.CardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsCardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsTable
import com.yaroslavzghoba.data.local.tables.UsersTable
import com.yaroslavzghoba.data.mappers.toCard
import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.model.CardSorting
import com.yaroslavzghoba.model.CardSortingColumn
import com.yaroslavzghoba.utils.suspendTransaction
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Represents a storage of cards in persistent memory.
 *
 * @throws IllegalArgumentException When trying to update a card with a `null` identifier.
 */
class CardStorageImpl : CardStorage {

    override suspend fun getById(id: Long): Card? = suspendTransaction {
        CardDao
            .find { CardsTable.id eq id }
            .map { it.toCard() }
            .firstOrNull()
    }

    override suspend fun getByOwnerId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<Card>> = suspendTransaction {
        val condition: SqlExpressionBuilder.() -> Op<Boolean> = {
            val baseCondition = CardsTable.ownerId eq id
            nextTimeBefore?.let {
                baseCondition and (CardsTable.showNextTimeAt neq null) and (CardsTable.showNextTimeAt lessEq it)
            } ?: baseCondition
        }

        // Apply filters and sorting to cards, create pairs of cards using limits and offsets,
        // and the total number of cards found without applying limits and offsets.
        val totalCountColumn = CardsTable.id.count().over().alias("total_count")
        val cardsWithTotalCount: List<Pair<Card, Long>> = CardsTable
            .select(columns = CardsTable.columns + totalCountColumn)
            .where(predicate = condition)
            .apply {
                val sorts = listOfNotNull(sortByFirstPriority, sortBySecondPriority)
                orderBy(*sorts.toTypedArray())
            }
            .limit(limit).offset(offset)
            .map { row ->
                Card(
                    id = row[CardsTable.id].value,
                    knownLanguageText = row[CardsTable.knownLanguageText],
                    learningLanguageText = row[CardsTable.learningLanguageText],
                    notes = row[CardsTable.notes],
                    lastAnsweredAt = row[CardsTable.lastAnsweredAt],
                    showNextTimeAt = row[CardsTable.showNextTimeAt],
                    correctAnswersInRow = row[CardsTable.correctAnswersInRow],
                    ownerId = row[CardsTable.ownerId].value,
                ) to row[totalCountColumn]
            }

        // Convert `List<Pair<Card, Long>>` to `Pair<Long, List<Card>>`
        // If the list of pairs is empty, then make another request to get the total number of cards
        // and return it with an empty list of cards.
        val result: Pair<Long, List<Card>> = if (cardsWithTotalCount.isEmpty()) {
            val totalCount = CardsTable.selectAll()
                .where(predicate = condition)
                .count()
            totalCount to emptyList()
        } else {
            val cards = cardsWithTotalCount.map { it.first }
            val totalCount = cardsWithTotalCount.first().second
            totalCount to cards
        }

        result
    }

    override suspend fun getByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long
    ): Pair<Long, List<Card>> = suspendTransaction {
        val condition: SqlExpressionBuilder.() -> Op<Boolean> = {
            val baseCondition = CollectionsCardsTable.collectionId eq id
            nextTimeBefore?.let {
                baseCondition and (CardsTable.showNextTimeAt neq null) and (CardsTable.showNextTimeAt lessEq it)
            } ?: baseCondition
        }

        // Apply filters and sorting to cards, create pairs of cards using limits and offsets,
        // and the total number of cards found without applying limits and offsets.
        val totalCountColumn = CardsTable.id.count().over().alias("total_count")
        val cardsWithTotalCount: List<Pair<Card, Long>> = CardsTable.innerJoin(CollectionsCardsTable)
            .select(CardsTable.columns + totalCountColumn)
            .where(predicate = condition)
            .withDistinct()
            .apply {
                val sorts = listOfNotNull(sortByFirstPriority, sortBySecondPriority)
                orderBy(*sorts.toTypedArray())
            }
            .limit(limit).offset(offset)
            .map { row ->
                Card(
                    id = row[CardsTable.id].value,
                    knownLanguageText = row[CardsTable.knownLanguageText],
                    learningLanguageText = row[CardsTable.learningLanguageText],
                    notes = row[CardsTable.notes],
                    lastAnsweredAt = row[CardsTable.lastAnsweredAt],
                    showNextTimeAt = row[CardsTable.showNextTimeAt],
                    correctAnswersInRow = row[CardsTable.correctAnswersInRow],
                    ownerId = row[CardsTable.ownerId].value,
                ) to row[totalCountColumn]
            }

        // Convert `List<Pair<Card, Long>>` to `Pair<Long, List<Card>>`
        // If the list of pairs is empty, then make another request to get the total number of cards
        // and return it with an empty list of cards.
        val result: Pair<Long, List<Card>> = if (cardsWithTotalCount.isEmpty()) {
            val totalCount = CardsTable.selectAll()
                .where(predicate = condition)
                .count()
            totalCount to emptyList()
        } else {
            val cards = cardsWithTotalCount.map { it.first }
            val totalCount = cardsWithTotalCount.first().second
            totalCount to cards
        }

        result
    }

    override suspend fun insert(card: Card): Card = suspendTransaction {
        // Get the card owner
        val owner = UserDao
            .find { UsersTable.id eq card.ownerId }
            .firstOrNull()
            ?: throw NoSuchElementException("The card owner not found in storage")

        CardDao.new(id = card.id) {
            knownLanguageText = card.knownLanguageText
            learningLanguageText = card.learningLanguageText
            notes = card.notes
            lastAnsweredAt = card.lastAnsweredAt
            showNextTimeAt = card.showNextTimeAt
            correctAnswersInRow = card.correctAnswersInRow
            ownerId = owner
        }.toCard()
    }

    override suspend fun update(card: Card): Card = suspendTransaction {
        if (card.id == null)
            throw IllegalArgumentException("The id of the card to be updated cannot be null")

        // Get the card owner
        val owner = UserDao
            .find { UsersTable.id eq card.ownerId }
            .firstOrNull()
            ?: throw NoSuchElementException("The card owner not found in storage")

        CardDao.findByIdAndUpdate(id = card.id) {
            it.knownLanguageText = card.knownLanguageText
            it.learningLanguageText = card.learningLanguageText
            it.notes = card.notes
            it.lastAnsweredAt = card.lastAnsweredAt
            it.showNextTimeAt = card.showNextTimeAt
            it.correctAnswersInRow = card.correctAnswersInRow
            it.ownerId = owner
        }?.toCard()
            ?: throw NoSuchElementException("Corresponding card is not found in storage")
    }

    override suspend fun addToCollection(
        card: Card,
        collection: CardCollection
    ): Card = suspendTransaction {
        if (card.id == null)
            throw IllegalArgumentException("The id of the card to be linked with collection cannot be null")
        if (collection.id == null)
            throw IllegalArgumentException("The id of the collection to be linked with card cannot be null")

        // Check if the card and the collection exist in the storage
        val correspondingCard = CardDao
            .find { CardsTable.id eq card.id }.firstOrNull()
            ?: throw NoSuchElementException("Corresponding card is not found in storage")
        val correspondingCollection = CollectionDao
            .find { CollectionsTable.id eq collection.id }.firstOrNull()
            ?: throw NoSuchElementException("Corresponding collection is not found in storage")

        // Check if the relationship already exists
        val relationship = CollectionCardDao
            .find {
                (CollectionsCardsTable.cardId eq card.id)
                    .and { CollectionsCardsTable.collectionId eq collection.id }
            }
            .firstOrNull()

        // Return if a relationship already exists
        if (relationship != null)
            throw IllegalStateException("The relationship between the card and the collection already exists.")

        CollectionCardDao.new {
            cardId = correspondingCard
            collectionId = correspondingCollection
        }
        card
    }

    override suspend fun removeFromCollection(
        card: Card,
        collection: CardCollection,
    ): Card = suspendTransaction {
        if (card.id == null)
            throw IllegalArgumentException("The id of the card to be linked with collection cannot be null")
        if (collection.id == null)
            throw IllegalArgumentException("The id of the collection to be linked with card cannot be null")

        CollectionCardDao
            .find {
                (CollectionsCardsTable.cardId eq card.id)
                    .and { CollectionsCardsTable.collectionId eq collection.id }
            }
            .toList()
            .forEach { relationship ->
                CollectionsCardsTable.deleteWhere {
                    CollectionsCardsTable.id eq relationship.id
                }
            }
        card
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        CardsTable.deleteAll()
    }

    override suspend fun deleteById(id: Long): Unit = suspendTransaction {
        CardsTable.deleteWhere {
            CardsTable.id eq id
        }
    }

    override suspend fun deleteByOwnerId(id: Long) {
        CardsTable.deleteWhere {
            CardsTable.ownerId eq id
        }
    }
}

/**
 * Returns a new [SizedIterable] with the cards sorted according to the [sorting].
 */
private fun Query.orderBy(vararg sorting: CardSorting): Query {
    val orders = sorting.map {
        it.column.toExpression() to it.order.toSqlSortOrder()
    }
    return orderBy(*orders.toTypedArray())
}

/**
 * Convert a card sort column to an expression with a corresponding column.
 *
 * @receiver An enum value of the [CardSortingColumn], representing a column in the table by which cards can be sorted.
 * @return An expression consisting of the corresponding column.
 */
private fun CardSortingColumn.toExpression(): Expression<*> = when (this) {
    CardSortingColumn.SHOW_NEXT_TIME_AT -> CardsTable.showNextTimeAt
}

/**
 * Convert the sort order received from the client to the appropriate sort order that can be used to create SQL queries.
 *
 * @receiver Sorting order received from the client.
 * @return An appropriate sort order that can be used to create SQL queries.
 */
private fun com.yaroslavzghoba.model.SortOrder.toSqlSortOrder(): SortOrder = when (this) {
    com.yaroslavzghoba.model.SortOrder.ASC -> SortOrder.ASC
    com.yaroslavzghoba.model.SortOrder.DESC -> SortOrder.DESC
}