package space.zghoba.data.local.tables

import org.jetbrains.exposed.dao.id.LongIdTable


/**
 * Represents a database table object that stores learning material usage examples.
 */
object ExamplesTable : LongIdTable(name = "examples", columnName = "id") {

    val knownLanguageText = text("known_language_text")
    val learningLanguageText = text("learning_language_text")
    val cardId = reference(name = "card_id", foreign = CardsTable)
}
