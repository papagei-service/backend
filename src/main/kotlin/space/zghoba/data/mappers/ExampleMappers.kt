package space.zghoba.data.mappers

import space.zghoba.data.local.dao.ExampleDao
import space.zghoba.model.Example

/**
 * Converts an instance of the [ExampleDao] class to an instance of the [Example] class.
 */
@Suppress("unused", "nothing_to_inline")
inline fun ExampleDao.toExample() = Example(
    id = this.id.value,
    knownLanguageText = this.knownLanguageText,
    learningLanguageText = this.learningLanguageText,
    cardId = this.cardId.id.value,
)