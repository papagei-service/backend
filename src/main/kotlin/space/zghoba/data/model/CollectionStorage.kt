package space.zghoba.data.model

import space.zghoba.model.CardCollection

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
     * Get a list of collections that owned by the user with an identifier equal to [id].
     *
     * @param id The unique identifier of a user whose collections are to be selected.
     * @param limit The maximal number of collections those will be returned.
     * @param offset Indicates how many collections should be skipped.
     *
     * @return Pair consisting of the total number of collections found and the collections themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getByOwnerId(
        id: Long,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<CardCollection>>

    /**
     * Get a list of collections that include the card with an identifier equal to [id].
     *
     * @param id The unique identifier of the card that belong to collections.
     * @param limit The maximal number of collections those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return Pair consisting of the total number of collections found and the collections themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getByCardId(
        id: Long,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<CardCollection>>

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

    /**
     * Delete all collections from the storage that owned by the user with an identifier equal to [id].
     */
    suspend fun deleteByOwnerId(id: Long)
}