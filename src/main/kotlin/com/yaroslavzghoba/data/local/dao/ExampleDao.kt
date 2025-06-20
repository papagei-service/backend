package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.ExamplesTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ExampleDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ExampleDao>(ExamplesTable)

    var knownLanguageText by ExamplesTable.knownLanguageText
    var learningLanguageText by ExamplesTable.learningLanguageText
    var cardId by CardDao referencedOn ExamplesTable
}