package com.yaroslavzghoba.data.local.tables

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Represents a database table object that stores card.
 */
object CardsTable : LongIdTable(name = "cards", columnName = "id") {

    val frontTitle = text(name = "front_title")
    val frontDescription = text(name = "front_description").nullable()
    val frontExample = text(name = "front_example").nullable()
    val backTitle = text(name = "back_title")
    val backDescription = text(name = "back_description").nullable()
    val backExample = text(name = "back_example").nullable()
    val nextTimeAt = timestamp(name = "next_time_at").default(Clock.System.now())
    val currentIntevalMs = long(name = "current_interval_ms")
    val collectionId = reference(name = "collection_id", foreign = CollectionsTable)
}