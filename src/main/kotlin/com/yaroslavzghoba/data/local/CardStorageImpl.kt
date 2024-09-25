package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.tables.CardsTable
import com.yaroslavzghoba.data.mappers.toCard
import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.model.Card
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of cards in persistent memory.
 *
 * @throws IllegalArgumentException When trying to update a card with a `null` identifier.
 */
class CardStorageImpl : CardStorage {

    override suspend fun getById(id: Long): Card? = CardDao
        .find { CardsTable.id eq id }
        .map { it.toCard() }
        .firstOrNull()

    override suspend fun getByCollectionId(id: Long): List<Card> = CardDao
        .find { CardsTable.collectionId eq id }
        .map { it.toCard() }

    override suspend fun insert(card: Card) {
        CardDao.new(id = card.id) {
            frontTitle = card.frontTitle
            frontDescription = card.frontDescription
            frontExample = card.frontExample
            backTitle = card.backTitle
            backDescription = card.backDescription
            backExample = card.backExample
            nextTimeAt = card.nextTimeAt
            currentIntervalMs = card.currentIntervalMs
            collectionId = card.collectionId
        }
    }

    override suspend fun update(card: Card) {
        if (card.id == null)
            throw IllegalArgumentException("The card with index `null` cannot be updated")
        CardDao.findByIdAndUpdate(id = card.id) {
            it.frontTitle = card.frontTitle
            it.frontDescription = card.frontDescription
            it.frontExample = card.frontExample
            it.backTitle = card.backTitle
            it.backDescription = card.backDescription
            it.backExample = card.backExample
            it.nextTimeAt = card.nextTimeAt
            it.currentIntervalMs = card.currentIntervalMs
            it.collectionId = card.collectionId
        }
    }

    override suspend fun deleteById(id: Long) {
        CardsTable.deleteWhere {
            CardsTable.id eq id
        }
    }
}