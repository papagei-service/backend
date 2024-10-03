package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.data.local.tables.CardsTable
import com.yaroslavzghoba.data.local.tables.CollectionsTable
import com.yaroslavzghoba.data.mappers.toCard
import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere

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

    override suspend fun getByCollectionId(id: Long): List<Card> = suspendTransaction {
        CardDao
            .find { CardsTable.collectionId eq id }
            .map { it.toCard() }
    }

    override suspend fun insert(card: Card): Card = suspendTransaction {
        // Get the parent collection of the card
        val collection = CollectionDao
            .find { CollectionsTable.id eq card.collectionId }
            .firstOrNull()
            ?: throw NoSuchElementException("Parent collection is not found in storage")

        CardDao.new(id = card.id) {
            frontTitle = card.frontTitle
            frontDescription = card.frontDescription
            frontExample = card.frontExample
            backTitle = card.backTitle
            backDescription = card.backDescription
            backExample = card.backExample
            nextTimeAt = card.nextTimeAt
            currentIntervalMs = card.currentIntervalMs
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
            it.frontTitle = card.frontTitle
            it.frontDescription = card.frontDescription
            it.frontExample = card.frontExample
            it.backTitle = card.backTitle
            it.backDescription = card.backDescription
            it.backExample = card.backExample
            it.nextTimeAt = card.nextTimeAt
            it.currentIntervalMs = card.currentIntervalMs
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