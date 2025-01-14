package com.yaroslavzghoba.model

/**
 * Represents a specific sorting for cards in the database.
 *
 * @param column A column by which cards are sorted.
 * @param order A column sorting order.
 */
data class CardSorting(
    val column: CardSortingColumn,
    val order: SortOrder,
)
