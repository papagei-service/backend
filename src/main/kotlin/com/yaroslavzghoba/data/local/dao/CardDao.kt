package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.CardsTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CardDao>(CardsTable)

    var nativeLanguageValue by CardsTable.nativeLanguageValue
    var nativeLanguageValueDescription by CardsTable.nativeLanguageValueDescription
    var nativeLanguageValueExample by CardsTable.nativeLanguageValueExample
    var foreignLanguageValue by CardsTable.foreignLanguageValue
    var foreignLanguageValueDescription by CardsTable.foreignLanguageValueDescription
    var foreignLanguageValueExample by CardsTable.foreignLanguageValueExample
    var nextTimeAt by CardsTable.nextTimeAt
    var correctAnswersInRow by CardsTable.correctAnswersInRow
    var collectionId by CollectionDao referencedOn CardsTable
}