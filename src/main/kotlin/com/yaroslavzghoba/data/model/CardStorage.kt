package com.yaroslavzghoba.data.model

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardSorting
import kotlinx.datetime.Instant

/**
 * Defines methods for storing and reading cards.
 */
interface CardStorage {

    /**
     * Get a card by its [id].
     *
     * @param id The unique identifier of the card by which the search is performed.
     * @return The card with the corresponding identifier or null if not found.
     */
    suspend fun getById(id: Long): Card?

    /**
     * Get a list of cards that belong to the card collection with an identifier equal to [id].
     *
     * @param id Unique identifier of the collection to which the requested cards belong.
     * @param sortByFirstPriority Options for the card sort that is performed first.
     * @param sortBySecondPriority Options for the card sort that is performed at the second stage
     * on cards with the same values in the column by which the first sort was performed.
     * @param nextTimeBefore The moment of time ahead of the time of the next repetition of the card.
     * @param limit The maximal number of cards those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return A list of cards that belong to the collection of cards with the [id] identifier.
     */
    suspend fun getByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting? = null,
        sortBySecondPriority: CardSorting? = null,
        nextTimeBefore: Instant? = null,
        limit: Int = 20,
        offset: Long = 0,
    ): List<Card>

    /**
     * Try to insert a card into the storage.
     *
     * @param card A card to be inserted into the storage.
     * @return Inserted card.
     *
     * @throws NoSuchElementException If the owner of the card is not found in the storage.
     */
    suspend fun insert(card: Card): Card

    /**
     * Try to update the card in the storage.
     *
     * @param card The card that must be updated.
     * @return Updated card.
     *
     * @throws IllegalArgumentException If the identifier of the passed card is null.
     * @throws NoSuchElementException If the parent collection is not found in the storage.
     */
    suspend fun update(card: Card): Card

    /**
     * Delete all cards from the storage.
     */
    suspend fun deleteAll()

    /**
     * Delete the card from the storage by its [id].
     *
     * @param id The unique identifier of the card that must be deleted.
     */
    suspend fun deleteById(id: Long)
}