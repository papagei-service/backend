package com.yaroslavzghoba.domain

import com.yaroslavzghoba.mappers.toCard
import com.yaroslavzghoba.model.CardAnswerDifficultyLevel
import com.yaroslavzghoba.utils.MockData
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// Unit tests for the HandleCardAnswerUseCase
class HandleCardAnswerUseCaseTest {

    private lateinit var useCase: HandleCardAnswerUseCase

    @BeforeTest
    fun setUp() {
        useCase = HandleCardAnswerUseCase()
    }

    @Test
    fun `=001 Reset correctAnswersInRow and set up next time if difficulty level is WRONG`() {
        val difficultyLevel = CardAnswerDifficultyLevel.WRONG
        val now = Clock.System.now()
        val initialCard = MockData.FIRST_CARD_REQUEST.toCard(id = 0, ownerId = 0)
            .copy(showNextTimeAt = now, correctAnswersInRow = 2)
        val actualCard = useCase.execute(card = initialCard, difficultyLevel = difficultyLevel)

        assertNotNull(actual = actualCard.lastAnsweredAt)
        assertNotNull(actual = actualCard.showNextTimeAt)
        assertEquals(expected = 0, actual = actualCard.correctAnswersInRow)
    }

    @Test
    fun `=002 Increment correctAnswersInRow and set up next time if difficulty level is not WRONG`() {
        val difficultyLevels = CardAnswerDifficultyLevel.entries
            .filter { it.name != CardAnswerDifficultyLevel.WRONG.name }

        difficultyLevels.forEach { difficultyLevel ->
            val now = Clock.System.now()
            val initialCard = MockData.FIRST_CARD_REQUEST.toCard(id = 0, ownerId = 0)
                .copy(showNextTimeAt = now, correctAnswersInRow = 2)
            val actualCard = useCase.execute(card = initialCard, difficultyLevel = difficultyLevel)

            assertNotNull(actual = actualCard.lastAnsweredAt)
            assertNotNull(actual = actualCard.showNextTimeAt)
            assertEquals(
                expected = initialCard.correctAnswersInRow + 1,
                actual = actualCard.correctAnswersInRow,
            )
            // The expected time of the last edit should match the actual time.
            assertEquals(
                expected = now.epochSeconds,
                actual = actualCard.lastAnsweredAt.epochSeconds
            )
            // Calculate actual duration
            val actualDuration = actualCard.showNextTimeAt - actualCard.lastAnsweredAt
            assertEquals(
                expected = (difficultyLevel.interval * actualCard.correctAnswersInRow).inWholeSeconds,
                actual = actualDuration.inWholeSeconds,
            )
        }
    }

    @Test
    fun `=003 Increase correctly the next time if the correctAnswersInRow is zero`() {
        val difficultyLevel = CardAnswerDifficultyLevel.WRONG
        val now = Clock.System.now()
        val initialCard = MockData.FIRST_CARD_REQUEST.toCard(id = 0, ownerId = 0)
            .copy(showNextTimeAt = now, correctAnswersInRow = 0)
        val actualCard = useCase.execute(card = initialCard, difficultyLevel = difficultyLevel)

        assertNotNull(actual = actualCard.lastAnsweredAt)
        assertNotNull(actual = actualCard.showNextTimeAt)
        assertEquals(expected = 0, actual = actualCard.correctAnswersInRow)
        // The expected time of the last edit should match the actual time.
        assertEquals(
            expected = now.epochSeconds,
            actual = actualCard.lastAnsweredAt.epochSeconds
        )
        // Calculate actual duration
        val actualDuration = actualCard.showNextTimeAt - actualCard.lastAnsweredAt
        assertEquals(
            expected = difficultyLevel.interval.inWholeSeconds,
            actual = actualDuration.inWholeSeconds,
        )
    }

    @Test
    fun `=004 Increase the next show time as the correctAnswersInRow increases`() {
        val difficultyLevel = CardAnswerDifficultyLevel.NORMAL
        val initialCard = MockData.FIRST_CARD_REQUEST.toCard(id = 0, ownerId = 0)

        // Same cards, but with different `correctAnswersInRow` parameters
        listOf(2, 3, 5).forEach { correctAnswersInRow ->
            val now = Clock.System.now()
            val actualCard = useCase.execute(
                card = initialCard.copy(showNextTimeAt = now, correctAnswersInRow = correctAnswersInRow),
                difficultyLevel = difficultyLevel,
            )

            assertNotNull(actual = actualCard.lastAnsweredAt)
            assertNotNull(actual = actualCard.showNextTimeAt)
            assertEquals(
                expected = correctAnswersInRow + 1,
                actual = actualCard.correctAnswersInRow,
            )
            // The expected time of the last edit should match the actual time.
            assertEquals(
                expected = now.epochSeconds,
                actual = actualCard.lastAnsweredAt.epochSeconds
            )
            // Calculate actual duration
            val actualDuration = actualCard.showNextTimeAt - actualCard.lastAnsweredAt
            assertEquals(
                expected = (difficultyLevel.interval * actualCard.correctAnswersInRow).inWholeSeconds,
                actual = actualDuration.inWholeSeconds,
            )
        }
    }

    @Test
    fun `=005 Do not modify other card's fields`() {
        val difficultyLevel = CardAnswerDifficultyLevel.WRONG
        val now = Clock.System.now()
        val initialCard = MockData.FIRST_CARD_REQUEST.toCard(id = 0, ownerId = 0)
            .copy(showNextTimeAt = now)
        val actualCard = useCase
            .execute(card = initialCard, difficultyLevel = difficultyLevel)
            .copy(
                lastAnsweredAt = initialCard.lastAnsweredAt,
                showNextTimeAt = initialCard.showNextTimeAt,
                correctAnswersInRow = initialCard.correctAnswersInRow,
            )
        assertEquals(
            expected = initialCard,
            actual = actualCard,
        )
    }
}
