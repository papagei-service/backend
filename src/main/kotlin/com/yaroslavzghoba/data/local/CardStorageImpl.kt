package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.dao.UserDao
import com.yaroslavzghoba.data.local.tables.CardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsCardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsTable
import com.yaroslavzghoba.data.mappers.toCard
import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.model.Card
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
    ): List<Card> = suspendTransaction {
        CardDao
            .find {
                val baseCondition = CardsTable.ownerId eq id
                nextTimeBefore?.let {
                    baseCondition and (CardsTable.showNextTimeAt lessEq it)
                } ?: baseCondition
            }
            .apply {
                val sorts = listOfNotNull(sortByFirstPriority, sortBySecondPriority)
                orderBy(*sorts.toTypedArray())
            }
            .limit(limit).offset(offset)
            .map { it.toCard() }
    }

    override suspend fun getByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long
    ): List<Card> = suspendTransaction {
        val baseCondition = CollectionsCardsTable.collectionId eq id
        val query = CardsTable.innerJoin(CollectionsCardsTable)
            .select(CardsTable.columns)
            .where {
                nextTimeBefore?.let {
                    baseCondition and (CardsTable.showNextTimeAt lessEq it)
                } ?: baseCondition
            }
            .withDistinct()

        CardDao.wrapRows(query)
            .apply {
                val sorts = listOfNotNull(sortByFirstPriority, sortBySecondPriority)
                orderBy(*sorts.toTypedArray())
            }
            .limit(limit).offset(offset)
            .toList().map { it.toCard() }
    }

    override suspend fun insert(card: Card): Card = suspendTransaction {
        // Get the card owner
        val owner = UserDao
            .find { CollectionsTable.id eq card.ownerId }
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
            .find { CollectionsTable.id eq card.ownerId }
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
private fun SizedIterable<CardDao>.orderBy(vararg sorting: CardSorting): SizedIterable<CardDao> {
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