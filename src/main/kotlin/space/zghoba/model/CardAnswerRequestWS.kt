package space.zghoba.model

/**
 * Represents an answer to a single card that sent by a client using Web-Sockets.
 *
 * @param difficultyLevel A name of the one of [CardAnswerDifficultyLevel].
 */
data class CardAnswerRequestWS(
    val difficultyLevel: String,
)