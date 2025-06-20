package com.yaroslavzghoba.data.local.tables

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * Represents a database table object that stores card collections.
 */
object CollectionsTable : LongIdTable(name = "collections", columnName = "id") {

    val title = text(name = "title")
    val description = text(name = "description").nullable()
    val knownLanguage = varchar(name = "known_language_iso_639_1", length = 2).nullable()
    val learningLanguage = varchar(name = "learning_language_iso_639_1", length = 2).nullable()
    val ownerId = reference(name = "owner_id", foreign = UsersTable)
}