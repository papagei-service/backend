package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.CollectionsTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CollectionDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CollectionDao>(CollectionsTable)

    var title by CollectionsTable.title
    var description by CollectionsTable.description
    var knownLanguage by CollectionsTable.knownLanguage
    var learningLanguage by CollectionsTable.learningLanguage
    var ownerId by UserDao referencedOn CollectionsTable.ownerId
}