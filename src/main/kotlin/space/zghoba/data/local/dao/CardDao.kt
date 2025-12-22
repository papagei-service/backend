package space.zghoba.data.local.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import space.zghoba.data.local.tables.CardsTable
import kotlin.time.ExperimentalTime

class CardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CardDao>(CardsTable)

    var knownLanguageText by CardsTable.knownLanguageText
    var learningLanguageText by CardsTable.learningLanguageText
    var notes by CardsTable.notes
    @OptIn(ExperimentalTime::class)
    var lastAnsweredAt by CardsTable.lastAnsweredAt
    @OptIn(ExperimentalTime::class)
    var showNextTimeAt by CardsTable.showNextTimeAt
    var correctAnswersInRow by CardsTable.correctAnswersInRow
    var ownerId by UserDao referencedOn CardsTable
}