package space.zghoba.data.local.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * Represents a database table object that stores card collections.
 */
object CollectionsCardsTable : LongIdTable(name = "collections_cards", columnName = "id") {

    val collectionId = reference(
        name = "collection_id",
        foreign = CollectionsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
    val cardId = reference(
        name = "card_id",
        foreign = CardsTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
}