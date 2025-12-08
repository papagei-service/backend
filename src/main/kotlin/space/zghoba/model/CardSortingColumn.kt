package space.zghoba.model

/**
 * Columns of the `Card` entity by which the list of cards can be sorted.
 */
enum class CardSortingColumn {
    ID,
    KNOWN_LANGUAGE_TEXT,
    LEARNING_LANGUAGE_TEXT,
    NOTES,
    LAST_ANSWERED_AT,
    SHOW_NEXT_TIME_AT,
    CORRECT_ANSWERS_IN_ROW,
    OWNER_ID
}