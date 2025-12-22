package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import space.zghoba.data.local.tables.CollectionsCardsTable

class CollectionCardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CollectionCardDao>(CollectionsCardsTable)

    var collectionId by CollectionDao referencedOn CollectionsCardsTable.collectionId
    var cardId by CardDao referencedOn CollectionsCardsTable.cardId
}