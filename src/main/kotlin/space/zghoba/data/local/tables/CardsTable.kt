package space.zghoba.data.local.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

/**
 * Represents a database table object that stores card.
 */
object CardsTable : LongIdTable(name = "cards", columnName = "id") {

    val knownLanguageText = text(name = "known_language_text")
    val learningLanguageText = text(name = "learning_language_text")
    val notes = text(name = "notes")
    @OptIn(ExperimentalTime::class)
    val lastAnsweredAt = timestamp(name = "last_answered_at").nullable()
    @OptIn(ExperimentalTime::class)
    val showNextTimeAt = timestamp(name = "show_next_time_at").nullable()
    val correctAnswersInRow = integer(name = "correct_answers_in_row").default(0)
    val ownerId = reference(
        name = "owner_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
}