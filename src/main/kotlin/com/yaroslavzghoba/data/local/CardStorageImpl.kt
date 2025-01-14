package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.tables.CardsTable
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

    override suspend fun getByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): List<Card> = suspendTransaction {
        CardDao
            .find {
                val baseCondition = CardsTable.collectionId eq id
                nextTimeBefore?.let {
                    baseCondition and (CardsTable.nextTimeAt lessEq it)
                } ?: baseCondition
            }
            .apply {
                val sorts = listOfNotNull(sortByFirstPriority, sortBySecondPriority)
                orderBy(*sorts.toTypedArray())
            }
            .limit(n = limit, offset = offset)
            .map { it.toCard() }
    }

    override suspend fun insert(card: Card): Card = suspendTransaction {
        // Get the parent collection of the card
        val collection = CollectionDao
            .find { CollectionsTable.id eq card.collectionId }
            .firstOrNull()
            ?: throw NoSuchElementException("Parent collection is not found in storage")

        CardDao.new(id = card.id) {
            nativeLanguageValue = card.nativeLanguageValue
            nativeLanguageValueDescription = card.nativeLanguageValueDescription
            nativeLanguageValueExample = card.nativeLanguageValueExample
            foreignLanguageValue = card.foreignLanguageValue
            foreignLanguageValueDescription = card.foreignLanguageValueDescription
            foreignLanguageValueExample = card.foreignLanguageValueExample
            nextTimeAt = card.nextTimeAt
            correctAnswersInRow = card.correctAnswersInRow
            collectionId = collection
        }.toCard()
    }

    override suspend fun update(card: Card): Card = suspendTransaction {
        if (card.id == null)
            throw IllegalArgumentException("The identifier of the card to be updated cannot be null")

        // Get the parent collection of the card
        val collection = CollectionDao
            .find { CollectionsTable.id eq card.collectionId }
            .firstOrNull()
            ?: throw NoSuchElementException("Parent collection is not found in storage")

        CardDao.findByIdAndUpdate(id = card.id) {
            it.nativeLanguageValue = card.nativeLanguageValue
            it.nativeLanguageValueDescription = card.nativeLanguageValueDescription
            it.nativeLanguageValueExample = card.nativeLanguageValueExample
            it.foreignLanguageValue = card.foreignLanguageValue
            it.foreignLanguageValueDescription = card.foreignLanguageValueDescription
            it.foreignLanguageValueExample = card.foreignLanguageValueExample
            it.nextTimeAt = card.nextTimeAt
            it.correctAnswersInRow = card.correctAnswersInRow
            it.collectionId = collection
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
    CardSortingColumn.NEXT_TIME_AT -> CardsTable.nextTimeAt
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