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
     * Get a list of collections owned by the owner with the [ownerId] id.
     *
     * @param ownerId The id of a user whose collections are to be selected.
     * @return List of collections owned by user with [ownerId] id.
     */
    suspend fun getByOwnerId(ownerId: Long): List<CardCollection>

    /**
     * Try to insert a collection into the storage.
     *
     * @param collection A collection to be inserted into the storage.
     * @return Inserted collection.
     *
     * @throws NoSuchElementException If the owner of the collection is not found in the storage.
     */
    suspend fun insert(collection: CardCollection): CardCollection

    /**
     * Try to update the collection in the storage.
     *
     * @param collection The collection that must be updated.
     * @return Updated collection.
     *
     * @throws IllegalArgumentException If the identifier of the passed collection is null.
     * @throws NoSuchElementException If the owner of the collection is not found in the storage.
     */
    suspend fun update(collection: CardCollection): CardCollection

    /**
     * Delete all collections from the storage.
     */
    suspend fun deleteAll()

    /**
     * Delete the collection from the storage by its [id].
     *
     * @param id The unique identifier of the collection that must be deleted.
     */
    suspend fun deleteById(id: Long)
}