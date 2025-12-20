package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an answer to a single card that sent by a client using Web-Sockets.
 *
 * @param difficultyLevel A name of the one of [CardAnswerDifficultyLevel].
 */
@Serializable
data class CardAnswerRequestWS(
    @SerialName("difficulty_level") val difficultyLevel: String,
)