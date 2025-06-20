package com.yaroslavzghoba.data.mappers

import com.yaroslavzghoba.data.local.dao.CollectionDao
import com.yaroslavzghoba.model.CardCollection

/**
 * Converts an instance of the [CollectionDao] class to an instance of the [CardCollection] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun CollectionDao.toCardCollection() = CardCollection(
    id = this.id.value,
    title = this.title,
    description = this.description,
    knownLanguage = this.knownLanguage,
    learningLanguage = this.learningLanguage,
    ownerId = this.ownerId.id.value,
)