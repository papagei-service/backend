package com.yaroslavzghoba.data.local.tables

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Represents a database table object that stores card.
 */
object CardsTable : LongIdTable(name = "cards", columnName = "id") {

    val nativeLanguageValue = text(name = "native_language_value")
    val nativeLanguageValueDescription = text(name = "native_language_value_description").nullable()
    val nativeLanguageValueExample = text(name = "native_language_value_example").nullable()
    val foreignLanguageValue = text(name = "foreign_language_value")
    val foreignLanguageValueDescription = text(name = "foreign_language_value_description").nullable()
    val foreignLanguageValueExample = text(name = "foreign_language_value_example").nullable()
    val nextTimeAt = timestamp(name = "next_time_at").default(Clock.System.now())
    val correctAnswersInRow = integer(name = "correct_answers_in_row")
    val collectionId = reference(name = "collection_id", foreign = CollectionsTable)
}