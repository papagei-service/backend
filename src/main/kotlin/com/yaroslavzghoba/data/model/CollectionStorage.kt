package com.yaroslavzghoba.data.model

import com.yaroslavzghoba.model.CardCollection

/**
 * Defines methods for storing and reading card collections.
 */
interface CollectionStorage {

    /**
     * Searches for a collection by its [id] in the storage. If the collection was found, the function returns it.
     * If the collection is not found, the method returns `null`.
     *
     * @param id A unique collection identifier that is used to search the collection.
     * @return The collection with the corresponding [id] if it was found and `null` if not.
     */
    suspend fun getById(id: Long): CardCollection?

    /**
     * Get a list of collections owned by the owner with the [ownerUsername] username.
     *
     * @param ownerUsername The username of a user whose card collections are to be selected.
     * @return List of card collections owned by user [ownerUsername].
     */
    suspend fun getByOwnerUsername(ownerUsername: String): List<CardCollection>

    /**
     * Try to insert a collection into the storage.
     *
     * @param collection A card collection to be inserted into the storage.
     * @throws IllegalArgumentException If there is already a collection with same id in the storage.
     */
    suspend fun insert(collection: CardCollection)

    /**
     * Try to update the card collection in the storage.
     *
     * @param collection The collection that must be updated.
     * @throws IllegalArgumentException If a collection with the same id is not found.
     */
    suspend fun update(collection: CardCollection)

    /**
     * Delete the card collection from the storage by its [id].
     *
     * @param id The unique identifier of the card collection that must be deleted.
     */
    suspend fun deleteById(id: Long)
}