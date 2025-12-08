package space.zghoba.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a flash card for learning a unit of information.
 *
 * @param id A unique identifier of the card.
 * @param knownLanguageText A word or phrase in a language that the user knows.
 * @param learningLanguageText A word or phrase in a language that the user wants to learn.
 * @param notes Additional notes on a word or phrase.
 * @param lastAnsweredAt UTC time when the user last answered to the card.
 * @param showNextTimeAt UTC time when the card should be shown next time.
 * @param correctAnswersInRow A number of correct answers in a row.
 * @param ownerId The unique identifier of the user to whom the cards belong.
 */
@Serializable
data class Card(
    @SerialName(CardFieldNames.ID) val id: Long?,
    @SerialName(CardFieldNames.KNOWN_LANGUAGE_TEXT) val knownLanguageText: String,
    @SerialName(CardFieldNames.LEARNING_LANGUAGE_TEXT) val learningLanguageText: String,
    @SerialName(CardFieldNames.NOTES) val notes: String,
    @SerialName(CardFieldNames.LAST_ANSWERED_AT) val lastAnsweredAt: Instant?,
    @SerialName(CardFieldNames.SHOW_NEXT_TIME_AT) val showNextTimeAt: Instant?,
    @SerialName(CardFieldNames.CORRECT_ANSWERS_IN_ROW) val correctAnswersInRow: Int,
    @SerialName(CardFieldNames.OWNER_ID) val ownerId: Long,
)
