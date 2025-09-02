package com.yaroslavzghoba.data.model

import com.yaroslavzghoba.model.Card
import com.yaroslavzghoba.model.CardCollection
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
     * Get a list of cards that owned by the user with an identifier equal to [id].
     *
     * @param id The unique identifier of the user to whom the cards belong.
     * @param sortByFirstPriority Options for the card sort that is performed first.
     * @param sortBySecondPriority Options for the card sort that is performed at the second stage
     * on cards with the same values in the column by which the first sort was performed.
     * @param nextTimeBefore The moment of time ahead of the time of the next repetition of the card.
     * @param limit The maximal number of cards those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return Pair consisting of the total number of cards found and the cards themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getByOwnerId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<Card>>

    /**
     * Get a list of cards included in the collection with an identifier equal to [id].
     *
     * @param id The unique identifier of the collection that include cards.
     * @param sortByFirstPriority Options for the card sort that is performed first.
     * @param sortBySecondPriority Options for the card sort that is performed at the second stage
     * on cards with the same values in the column by which the first sort was performed.
     * @param nextTimeBefore The moment of time ahead of the time of the next repetition of the card.
     * @param limit The maximal number of cards those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return Pair consisting of the total number of cards found and the cards themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getByCollectionId(
        id: Long,
        sortByFirstPriority: CardSorting?,
        sortBySecondPriority: CardSorting?,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<Card>>

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
     */
    suspend fun update(card: Card): Card

    /**
     * Register the card as part of a collection.
     *
     * @param card Card to be added to the collection.
     * @param collection Collection to which the card will be added.
     *
     * @throws IllegalArgumentException If the identifier of the passed card or collection is null.
     * @throws NoSuchElementException If the corresponding card or collection is not found in storage.
     * @throws IllegalStateException If a relationship between the card and the collection already exists.
     */
    suspend fun addToCollection(card: Card, collection: CardCollection): Card

    /**
     * Break the relationship between cards and collections
     *
     * @param card Card to be removed from the collection.
     * @param collection Collection from which the card will be removed.
     *
     * @throws IllegalArgumentException If the identifier of the passed card or collection is null.
     */
    suspend fun removeFromCollection(card: Card, collection: CardCollection): Card

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

    /**
     * Delete all cards from the storage that owned by the user with an identifier equal to [id].
     */
    suspend fun deleteByOwnerId(id: Long)
}