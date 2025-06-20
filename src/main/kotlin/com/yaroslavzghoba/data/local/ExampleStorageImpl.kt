package com.yaroslavzghoba.data.local

import com.yaroslavzghoba.data.local.dao.CardDao
import com.yaroslavzghoba.data.local.dao.ExampleDao
import com.yaroslavzghoba.data.local.tables.CardsTable
import com.yaroslavzghoba.data.local.tables.ExamplesTable
import com.yaroslavzghoba.data.mappers.toExample
import com.yaroslavzghoba.data.model.ExampleStorage
import com.yaroslavzghoba.model.Example
import com.yaroslavzghoba.utils.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere

/**
 * Represents a storage of card learning material usage examples in persistent memory.
 *
 * @throws IllegalArgumentException When trying to update an example with a `null` identifier.
 */
class ExampleStorageImpl : ExampleStorage {

    override suspend fun getById(id: Long): Example? = suspendTransaction {
        ExampleDao
            .find { ExamplesTable.id eq id }
            .map { it.toExample() }
            .firstOrNull()
    }

    override suspend fun getByCardId(
        id: Long,
        limit: Int,
        offset: Long
    ): List<Example> = suspendTransaction {
        ExampleDao
            .find { ExamplesTable.cardId eq id }
            .limit(limit).offset(offset)
            .map { it.toExample() }
    }

    override suspend fun insert(example: Example): Example = suspendTransaction {
        // Get the parent card
        val parentCard = CardDao
            .find { CardsTable.id eq example.cardId }
            .firstOrNull()
            ?: throw NoSuchElementException("The parent card of the example not found in storage")

        ExampleDao.new(id = example.id) {
            knownLanguageText = example.knownLanguageText
            learningLanguageText = example.learningLanguageText
            cardId = parentCard
        }.toExample()
    }

    override suspend fun update(example: Example): Example = suspendTransaction {
        if (example.id == null)
            throw IllegalArgumentException("The id of the example to be updated cannot be null")

        // Get the parent card
        val parentCard = CardDao
            .find { CardsTable.id eq example.cardId }
            .firstOrNull()
            ?: throw NoSuchElementException("The parent card of the example not found in storage")

        ExampleDao.findByIdAndUpdate(id = example.id) {
            it.knownLanguageText = example.knownLanguageText
            it.learningLanguageText = example.learningLanguageText
            it.cardId = parentCard
        }?.toExample()
            ?: throw NoSuchElementException("Corresponding example is not found in storage")
    }

    override suspend fun deleteAll(): Unit = suspendTransaction {
        ExamplesTable.deleteAll()
    }

    override suspend fun deleteById(id: Long): Unit = suspendTransaction {
        ExamplesTable.deleteWhere {
            ExamplesTable.id eq id
        }
    }

    override suspend fun deleteByCardId(id: Long): Unit = suspendTransaction {
        ExamplesTable.deleteWhere {
            ExamplesTable.cardId eq id
        }
    }
}