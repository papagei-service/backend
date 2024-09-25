package com.yaroslavzghoba.data.model

import com.yaroslavzghoba.model.Card

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
     * @return A list of cards that belong to the collection of cards with the [id] identifier.
     */
    suspend fun getByCollectionId(id: Long): List<Card>

    /**
     * Try to insert a card into the storage.
     *
     * @param card A card to be inserted into the storage.
     * @throws IllegalArgumentException If there is already a card with same id in the storage.
     */
    suspend fun insert(card: Card)

    /**
     * Try to update the card in the storage.
     *
     * @param card The card that must be updated.
     * @throws IllegalArgumentException If a card with the same id is not found.
     */
    suspend fun update(card: Card)

    /**
     * Delete the card from the storage by its [id].
     *
     * @param id The unique identifier of the card that must be deleted.
     */
    suspend fun deleteById(id: Long)
}