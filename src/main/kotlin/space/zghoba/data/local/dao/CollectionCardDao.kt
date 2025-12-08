package space.zghoba.data.local.dao

import space.zghoba.data.local.tables.CollectionsCardsTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CollectionCardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CollectionCardDao>(CollectionsCardsTable)

    var collectionId by CollectionDao referencedOn CollectionsCardsTable.collectionId
    var cardId by CardDao referencedOn CollectionsCardsTable.cardId
}