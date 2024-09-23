package com.yaroslavzghoba.model

/**
 * Defines methods for storing and reading user data.
 */
interface Repository {

    /**
     * Searches for a user by its [username] in the storage. If the user was found, the function returns it.
     * If the user is not found, the method returns `null`.
     *
     * @param username A unique user identifier that is used to search.
     * @return The user with the corresponding [username] if it was found and `null` if not.
     */
    suspend fun getUserByUsername(username: String): User?

    /**
     * Try to insert a user into the storage.
     *
     * @param user The user that must be inserted to the storage.
     * @throws IllegalArgumentException if the user with the same username is already exists in the storage.
     */
    suspend fun insertUser(user: User)

    /**
     * Try to update the user in the storage.
     *
     * @param user The user that must be updated.
     * @throws IllegalArgumentException If a user with the same username is not found.
     */
    suspend fun updateUser(user: User)

    /**
     * Delete the user from the storage by its username.
     *
     * @param username The username of the user to be deleted.
     */
    suspend fun deleteUserByUsername(username: String)

    /**
     * Searches for a collection by its [id] in the storage. If the collection was found, the function returns it.
     * If the collection is not found, the method returns `null`.
     *
     * @param id A unique collection identifier that is used to search the collection.
     * @return The collection with the corresponding [id] if it was found and `null` if not.
     */
    suspend fun getCollectionById(id: Long): CardCollection?

    /**
     * Get a list of collections owned by the owner with the [ownerUsername] username.
     *
     * @param ownerUsername The username of a user whose card collections are to be selected.
     * @return List of card collections owned by user [ownerUsername].
     */
    suspend fun getCollectionsByOwnerUsername(ownerUsername: String): List<CardCollection>

    /**
     * Try to insert a collection into the storage.
     *
     * @param collection A card collection to be inserted into the storage.
     * @throws IllegalArgumentException If there is already a collection with same id in the storage.
     */
    suspend fun insertCollection(collection: CardCollection)

    /**
     * Try to update the card collection in the storage.
     *
     * @param collection The collection that must be updated.
     * @throws IllegalArgumentException If a collection with the same id is not found.
     */
    suspend fun updateCollection(collection: CardCollection)

    /**
     * Delete the card collection from the storage by its [id].
     *
     * @param id The unique identifier of the card collection that must be deleted.
     */
    suspend fun deleteCollectionById(id: Long)

    /**
     * Get a card by its [id].
     *
     * @param id The unique identifier of the card by which the search is performed.
     * @return The card with the corresponding identifier or null if not found.
     */
    suspend fun getCardById(id: Long): Card?

    /**
     * Get a list of cards that belong to the card collection with an identifier equal to [id].
     *
     * @param id Unique identifier of the collection to which the requested cards belong.
     * @return A list of cards that belong to the collection of cards with the [id] identifier.
     */
    suspend fun getCardsByCollectionId(id: Long): List<Card>

    /**
     * Try to insert a card into the storage.
     *
     * @param card A card to be inserted into the storage.
     * @throws IllegalArgumentException If there is already a card with same id in the storage.
     */
    suspend fun insertCard(card: Card)

    /**
     * Try to update the card in the storage.
     *
     * @param card The card that must be updated.
     * @throws IllegalArgumentException If a card with the same id is not found.
     */
    suspend fun updateCard(card: Card)

    /**
     * Delete the card from the storage by its [id].
     *
     * @param id The unique identifier of the card that must be deleted.
     */
    suspend fun deleteCardById(id: Long)
}