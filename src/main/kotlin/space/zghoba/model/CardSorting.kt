package space.zghoba.model

/**
 * Represents a specific sorting for cards in the database.
 *
 * @param column A column by which cards are sorted.
 * @param order A column sorting order.
 */
data class CardSorting(
    val column: CardSortingColumn,
    val order: SortOrder,
) {
    companion object {

        /**
         * Convert a string that contains a sort order symbol and a card column name to the [CardSorting] class.
         *
         * @param string String containing a sort order symbol and a card column name.
         * @return Instance of the [CardSorting] class containing the corresponding card column and sorting order.
         *
         * @throws IllegalArgumentException if:
         *   - The passed string is empty or contains only spaces.
         *   - The extracted order is not valid.
         *   - The extracted card column is not valid or cannot be used in sorting.
         *
         * @sample getCardSortingFromString
         */
        fun fromString(string: String): CardSorting {
            val trimmedString = string.trim()
            require(trimmedString.isNotEmpty()) { "The string must not be blank." }

            val order = when (val order = trimmedString.first()) {
                '+' -> SortOrder.ASC
                '-' -> SortOrder.DESC
                else -> throw IllegalArgumentException("The order sign is not valid. The order: $order.")
            }

            val column = when (val column = trimmedString.substring(startIndex = 1)) {
                CardFieldNames.ID -> CardSortingColumn.ID
                CardFieldNames.KNOWN_LANGUAGE_TEXT -> CardSortingColumn.KNOWN_LANGUAGE_TEXT
                CardFieldNames.LEARNING_LANGUAGE_TEXT -> CardSortingColumn.LEARNING_LANGUAGE_TEXT
                CardFieldNames.NOTES -> CardSortingColumn.NOTES
                CardFieldNames.LAST_ANSWERED_AT -> CardSortingColumn.LAST_ANSWERED_AT
                CardFieldNames.SHOW_NEXT_TIME_AT -> CardSortingColumn.SHOW_NEXT_TIME_AT
                CardFieldNames.CORRECT_ANSWERS_IN_ROW -> CardSortingColumn.CORRECT_ANSWERS_IN_ROW
                CardFieldNames.OWNER_ID -> CardSortingColumn.OWNER_ID
                else -> throw IllegalArgumentException(
                    "The column is not valid or cannot be used in sorting. The column: $column."
                )
            }

            return CardSorting(column = column, order = order)
        }

        /**
         * Convert a string that contains a sort order symbol and a card column name to the [CardSorting] class.
         * If the [string] cannot be converted, then return null.
         *
         * @param string String containing a sort order symbol and a card column name.
         * @return Instance of the [CardSorting] class containing the corresponding card column and sorting order
         * or null it the string cannot be converted to the [CardSorting].
         *
         * @sample getCardSortingOrNullFromString
         */
        fun fromStringOrNull(string: String): CardSorting? {
            return try {
                this.fromString(string = string)
            } catch (_: Exception) {
                null
            }
        }
    }
}

@Suppress("unused")
private fun getCardSortingFromString() {

    // output: CardSorting(
    //     column=CardSortingColumn.SHOW_NEXT_TIME_AT,
    //     order=SortOrder.DESC
    // )
    val sorting1 = CardSorting.fromString(" -show_next_time_at")
    println(sorting1)

    // output: CardSorting(
    //     column=CardSortingColumn.KNOWN_LANGUAGE_TEXT,
    //     order=SortOrder.ASC
    // )
    val sorting2 = CardSorting.fromString("+known_language_text  ")
    println(sorting2)
}

@Suppress("unused")
private fun getCardSortingOrNullFromString() {

    // output: CardSorting(
    //     column=CardSortingColumn.SHOW_NEXT_TIME_AT,
    //     order=SortOrder.DESC
    // )
    val sorting1 = CardSorting.fromStringOrNull(" -show_next_time_at")
    println(sorting1)

    // output: CardSorting(
    //     column=CardSortingColumn.KNOWN_LANGUAGE_TEXT,
    //     order=SortOrder.ASC
    // )
    val sorting2 = CardSorting.fromStringOrNull("+known_language_text  ")
    println(sorting2)

    // output: null
    val sorting3 = CardSorting.fromStringOrNull("=lorem_ipsum")
    println(sorting3)
}
