package space.zghoba.domain

import space.zghoba.model.Card
import space.zghoba.model.CardAnswerDifficultyLevel
import kotlinx.datetime.Clock

/**
 * Update the card using the user's answer to it.
 */
class HandleCardAnswerUseCase() {

    /**
     * Modify the next card show time according to the user's answer.
     *
     * @param card The card to which the answer is assigned.
     * @param difficultyLevel Level of difficulty in recalling the correct answer.
     *
     * @return Updated card.
     */
    fun execute(card: Card, difficultyLevel: CardAnswerDifficultyLevel): Card {
        val now = Clock.System.now()
        // Increase the `correctAnswersInRow` if correct and reset if wrong
        val isCorrect = difficultyLevel != CardAnswerDifficultyLevel.WRONG
        val correctAnswersInRow = if (isCorrect) card.correctAnswersInRow + 1 else 0
        // Increase the `showNextTimeAt`
        val showNextTimeAt = now + difficultyLevel.interval *
                (if (correctAnswersInRow == 0) 1 else correctAnswersInRow)

        return card.copy(
            lastAnsweredAt = now,
            showNextTimeAt = showNextTimeAt,
            correctAnswersInRow = correctAnswersInRow,
        )
    }
}