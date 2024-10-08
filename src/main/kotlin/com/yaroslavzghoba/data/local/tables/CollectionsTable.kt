package com.yaroslavzghoba.data.local.tables

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * Represents a database table object that stores card collections.
 */
object CollectionsTable : LongIdTable(name = "collections", columnName = "id") {

    val title = text(name = "title")
    val description = text(name = "description").nullable()
    val subjectType = varchar(name = "subject_type", length = 32)
    val subjectLanguage = varchar(name = "subject_language", length = 2).nullable()
    val nativeLanguage = varchar(name = "native_language", length = 2).nullable()
    val ownerUsername = reference(name = "owner_username", foreign = UsersTable)
}