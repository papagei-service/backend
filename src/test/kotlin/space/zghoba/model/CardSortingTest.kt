package space.zghoba.model

import org.junit.jupiter.api.assertNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CardSortingTest {

    @Test
    fun `Do not convert the string to the CardSorting if it is blank`() {
        val string = "   "
        assertFailsWith(exceptionClass = IllegalArgumentException::class) {
            CardSorting.fromString(string = string)
        }
    }

    @Test
    fun `Do not convert the string to the CardSorting if the order sign is invalid`() {
        val string = "=${CardFieldNames.CORRECT_ANSWERS_IN_ROW}"
        assertFailsWith(exceptionClass = IllegalArgumentException::class) {
            CardSorting.fromString(string = string)
        }
    }

    @Test
    fun `Do not convert the string to the CardSorting if the column name doesn't exist`() {
        val string = "+wrong_column_name"
        assertFailsWith(exceptionClass = IllegalArgumentException::class) {
            CardSorting.fromString(string = string)
        }
    }

    @Test
    fun `Do not distort data after conversion the string to the CardSorting`() {
        assertEquals(
            expected = CardSorting(
                column = CardSortingColumn.KNOWN_LANGUAGE_TEXT,
                order = SortOrder.ASC,
            ),
            actual = CardSorting.fromString("+${CardFieldNames.KNOWN_LANGUAGE_TEXT}"),
        )
        assertEquals(
            expected = CardSorting(
                column = CardSortingColumn.CORRECT_ANSWERS_IN_ROW,
                order = SortOrder.DESC,
            ),
            actual = CardSorting.fromString("-${CardFieldNames.CORRECT_ANSWERS_IN_ROW}"),
        )
    }

    @Test
    fun `Convert the untrimmed string to the CardSorting`() {
        val string = "   +${CardFieldNames.LAST_ANSWERED_AT}  "
        val cardSorting = CardSorting(
            column = CardSortingColumn.LAST_ANSWERED_AT,
            order = SortOrder.ASC,
        )
        assertEquals(
            expected = cardSorting,
            actual = CardSorting.fromString(string),
        )
    }

    @Test
    fun `Return null if the string cannot be converted to the CardSorting`() {
        val invalidStrings = listOf("   ", "=${CardFieldNames.ID}", "+wrong_column_name")
        invalidStrings.forEach { invalidString ->
            val result = CardSorting.fromStringOrNull(string = invalidString)
            assertNull(actual = result)
        }
    }
}