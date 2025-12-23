package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.zghoba.utils.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents the body of a request to insert a card.
 *
 * @param knownLanguageText A word or phrase in a language that the user knows.
 * @param learningLanguageText A word or phrase in a language that the user wants to learn.
 * @param notes Additional notes on a word or phrase.
 * @param lastAnsweredAt UTC time when the user last answered to the card.
 * @param showNextTimeAt UTC time when the card should be shown next time.
 * @param correctAnswersInRow A number of correct answers in a row.
 */
@Serializable
data class CardInsertRequest @OptIn(ExperimentalTime::class) constructor(
    @SerialName(CardFieldNames.KNOWN_LANGUAGE_TEXT)
    val knownLanguageText: String,

    @SerialName(CardFieldNames.LEARNING_LANGUAGE_TEXT)
    val learningLanguageText: String,

    @SerialName(CardFieldNames.NOTES)
    val notes: String,

    @Serializable(with = InstantSerializer::class)
    @SerialName(CardFieldNames.LAST_ANSWERED_AT)
    val lastAnsweredAt: Instant?,

    @Serializable(with = InstantSerializer::class)
    @SerialName(CardFieldNames.SHOW_NEXT_TIME_AT)
    val showNextTimeAt: Instant?,

    @SerialName(CardFieldNames.CORRECT_ANSWERS_IN_ROW)
    val correctAnswersInRow: Int,
)