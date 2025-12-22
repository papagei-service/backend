package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import space.zghoba.data.local.tables.ExamplesTable

class ExampleDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ExampleDao>(ExamplesTable)

    var knownLanguageText by ExamplesTable.knownLanguageText
    var learningLanguageText by ExamplesTable.learningLanguageText
    var cardId by CardDao referencedOn ExamplesTable
}