package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.model.CardCollection
import com.yaroslavzghoba.utils.SubjectType

/**
 * Converts an instance of the [CollectionDao] class to an instance of the [CardCollection] class.
 */
@Suppress("unused")
fun CollectionDao.toCardCollection() = CardCollection(
    id = this.id.value,
    title = this.title,
    description = this.description,
    subjectType = SubjectType.valueOf(this.subjectType),
    subjectLanguage = this.subjectLanguage,
    nativeLanguage = this.nativeLanguage,
    ownerUsername = this.ownerUsername,
)