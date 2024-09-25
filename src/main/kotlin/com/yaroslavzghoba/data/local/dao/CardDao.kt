package com.yaroslavzghoba.data.local.dao

import com.yaroslavzghoba.data.local.tables.CardsTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CardDao(id: EntityID<Long>) : LongEntity(id = id) {
    companion object : LongEntityClass<CardDao>(CardsTable)

    var frontTitle by CardsTable.frontTitle
    var frontDescription by CardsTable.frontDescription
    var frontExample by CardsTable.frontExample
    var backTitle by CardsTable.backTitle
    var backDescription by CardsTable.backDescription
    var backExample by CardsTable.backExample
    var nextTimeAt by CardsTable.nextTimeAt
    var currentIntervalMs by CardsTable.currentIntevalMs
    var collectionId by CardsTable.collectionId
}