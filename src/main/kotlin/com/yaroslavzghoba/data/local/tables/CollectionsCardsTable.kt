package com.yaroslavzghoba.data.local.tables

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * Represents a database table object that stores card collections.
 */
object CollectionsCardsTable : LongIdTable(name = "collections_cards", columnName = "id") {

    val collectionId = reference(name = "collection_id", foreign = CollectionsTable)
    val cardId = reference(name = "card_id", foreign = CardsTable)
}