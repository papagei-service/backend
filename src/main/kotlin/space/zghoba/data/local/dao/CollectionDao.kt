package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import space.zghoba.data.local.tables.CollectionsTable

class CollectionDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CollectionDao>(CollectionsTable)

    var title by CollectionsTable.title
    var description by CollectionsTable.description
    var knownLanguage by CollectionsTable.knownLanguage
    var learningLanguage by CollectionsTable.learningLanguage
    var ownerId by UserDao referencedOn CollectionsTable.ownerId
}