package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.CardsTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CardDao>(CardsTable)

    var knownLanguageText by CardsTable.knowsLanguageText
    var learningLanguageText by CardsTable.learningLanguageText
    var notes by CardsTable.notes
    var lastAnsweredAt by CardsTable.lastAnsweredAt
    var showNextTimeAt by CardsTable.showNextTimeAt
    var correctAnswersInRow by CardsTable.correctAnswersInRow
    var ownerId by UserDao referencedOn CardsTable
}