package com.yaroslavzghoba.model

import kotlinx.datetime.Instant

/**
 * Defines methods for storing and reading user data.
 */
interface Repository {

    /**
     * Searches for a user by its [id] in the storage. If the user was found, the function returns it.
     * If the user is not found, the method returns null.
     *
     * @param id A unique immutable user identifier that is used to search.
     * @return The user with the corresponding [id] if it was found and null if not.
     */
    suspend fun getUserById(id: Long): User?

    /**
     * Searches for a user by its [username] in the storage. If the user was found, the function returns it.
     * If the user is not found, the method returns null.
     *
     * @param username A unique mutable user identifier that is used to search.
     * @return The user with the corresponding [username] if it was found and null if not.
     */
    suspend fun getUserByUsername(username: String): User?

    /**
     * Try to insert a user into the storage.
     *
     * @param user The user that must be inserted to the storage.
     * @return Inserted user.
     *
     * @throws IllegalArgumentException if the user with the same username is already exists in the storage.
     */
    suspend fun insertUser(user: User): User

    /**
     * Try to update the user in the storage.
     *
     * @param user The user that must be updated.
     * @return Updated user.
     *
     * @throws IllegalArgumentException If a user with the same username is not found.
     */
    suspend fun updateUser(user: User): User

    /**
     * Delete all users from the storage.
     */
    suspend fun deleteAllUsers()

    /**
     * Delete the user from the storage by its id.
     *
     * @param id The unique identifier of the user to be deleted.
     */
    suspend fun deleteUserById(id: Long)

    /**
     * Searches for a collection by its [id] in the storage. If the collection was found, the function returns it.
     * If the collection is not found, the method returns `null`.
     *
     * @param id A unique collection identifier that is used to search the collection.
     * @return The collection with the corresponding [id] if it was found and `null` if not.
     */
    suspend fun getCollectionById(id: Long): CardCollection?

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
    suspend fun getCollectionsByOwnerId(
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
    suspend fun getCollectionsByCardId(
        id: Long,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<CardCollection>>

    /**
     * Try to insert a collection into the storage.
     *
     * @param collection A card collection to be inserted into the storage.
     * @return Inserted collection.
     *
     * @throws IllegalArgumentException If there is already a collection with same id in the storage.
     * @throws NoSuchElementException If the owner of the collection is not found in the storage.
     */
    suspend fun insertCollection(collection: CardCollection): CardCollection

    /**
     * Try to update the card collection in the storage.
     * @return Updated collection
     *
     * @param collection The collection that must be updated.
     * @throws IllegalArgumentException If a collection with the same id is not found.
     */
    suspend fun updateCollection(collection: CardCollection): CardCollection

    /**
     * Delete all card collections from the storage.
     */
    suspend fun deleteAllCollections()

    /**
     * Delete the card collection from the storage by its [id].
     *
     * @param id The unique identifier of the card collection that must be deleted.
     */
    suspend fun deleteCollectionById(id: Long)

    /**
     * Delete all card collections from the storage that owned by the user with an identifier equal to [id].
     */
    suspend fun deleteCollectionsByOwnerId(id: Long)

    /**
     * Get a card by its [id].
     *
     * @param id The unique identifier of the card by which the search is performed.
     * @return The card with the corresponding identifier or null if not found.
     */
    suspend fun getCardById(id: Long): Card?

    /**
     * Get a list of cards that owned by the user with an identifier equal to [id].
     *
     * @param id Unique identifier of the user that own the requested cards.
     * @param sortings Options for sorting cards. The sorting order will be the same
     * as the order in which the cards are listed.
     * @param nextTimeBefore The moment of time ahead of the time of the next repetition of the card.
     * @param limit The maximal number of cards those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return Pair consisting of the total number of cards found and the cards themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getCardsByOwnerId(
        id: Long,
        sortings: List<CardSorting>,
        nextTimeBefore: Instant?,
        limit: Int,
        offset: Long,
    ): Pair<Long, List<Card>>

    /**
     * Get a list of cards included in the collection with an identifier equal to [id].
     *
     * @param id The unique identifier of the collection that include cards.
     * @param sortings Options for sorting cards. The sorting order will be the same
     * as the order in which the cards are listed.
     * @param nextTimeBefore The moment of time ahead of the time of the next repetition of the card.
     * @param limit The maximal number of cards those will be returned.
     * @param offset Indicates how many cards should be skipped.
     *
     * @return Pair consisting of the total number of cards found and the cards themselves,
     * to which the limit and offset are also applied.
     */
    suspend fun getCardsByCollectionId(
        id: Long,
        sortings: List<CardSorting>,
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
     * @throws IllegalArgumentException If there is already a card with same id in the storage.
     */
    suspend fun insertCard(card: Card): Card

    /**
     * Try to update the card in the storage.
     *
     * @param card The card that must be updated.
     * @return Updated card.
     *
     * @throws IllegalArgumentException If a card with the same id is not found.
     * @throws NoSuchElementException If the parent collection is not found in the storage.
     */
    suspend fun updateCard(card: Card): Card

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
    suspend fun addCardToCollection(card: Card, collection: CardCollection): Card

    /**
     * Break the relationship between cards and collections
     *
     * @param card Card to be removed from the collection.
     * @param collection Collection from which the card will be removed.
     *
     * @throws IllegalArgumentException If the identifier of the passed card or collection is null.
     */
    suspend fun removeCardFromCollection(card: Card, collection: CardCollection): Card

    /**
     * Delete all cards from the storage.
     */
    suspend fun deleteAllCards()

    /**
     * Delete the card from the storage by its [id].
     *
     * @param id The unique identifier of the card that must be deleted.
     */
    suspend fun deleteCardById(id: Long)

    /**
     * Delete all cards from the storage that owned by the user with an identifier equal to [id].
     */
    suspend fun deleteCardsByOwnerId(id: Long)

    /**
     * Get an example by its [id].
     *
     * @param id The unique identifier of the example by which the search is performed.
     * @return The example with the corresponding identifiers or null if not found.
     */
    suspend fun getExampleById(id: Long): Example?

    /**
     * Get a list of examples that belong to the card with an identifier equal to [id].
     *
     * @param id The unique identifier of the card to which the examples belong.
     * @param limit The maximal number of examples those will be returned.
     * @param offset Indicates how many examples should be skipped.
     *
     * @return A list of examples that belong to the card with the [id] identifier.
     */
    suspend fun getExamplesByCardId(id: Long, limit: Int, offset: Long): List<Example>

    /**
     * Try to insert an example into the storage.
     *
     * @param example An example to be inserted into the storage.
     * @return Inserted example.
     *
     * @throws NoSuchElementException If the card to which the example belongs is not found in the storage.
     */
    suspend fun insertExample(example: Example): Example

    /**
     * Try to update the example in the storage.
     *
     * @param example The example that must be updated.
     * @return Updated example.
     *
     * @throws IllegalArgumentException If the identifier of the passed example is null.
     * @throws NoSuchElementException If the parent card is not found in the storage.
     */
    suspend fun updateExample(example: Example): Example

    /**
     * Delete all examples from the storage.
     */
    suspend fun deleteAllExamples()

    /**
     * Delete the example from the storage by its [id].
     *
     * @param id The unique identifier of the example that must be deleted.
     */
    suspend fun deleteExampleById(id: Long)

    /**
     * Delete all examples from the storage that belong to the card with an identifier equal to [id].
     *
     * @param id The unique identifier of the card to which examples belong.
     */
    suspend fun deleteExampleByCardId(id: Long)

    /**
     * Clear all rows in all storage tables.
     */
    suspend fun clear()
}