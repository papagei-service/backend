package space.zghoba.data.model

import space.zghoba.model.Example

/**
 * Defines methods for storing and reading learning material usage examples.
 */
interface ExampleStorage {

    /**
     * Get an example by its [id].
     *
     * @param id The unique identifier of the example by which the search is performed.
     * @return The example with the corresponding identifiers or null if not found.
     */
    suspend fun getById(id: Long): Example?

    /**
     * Get a list of examples that belong to the card with an identifier equal to [id].
     *
     * @param id The unique identifier of the card to which the examples belong.
     * @param limit The maximal number of examples those will be returned.
     * @param offset Indicates how many examples should be skipped.
     *
     * @return A list of examples that belong to the card with the [id] identifier.
     */
    suspend fun getByCardId(id: Long, limit: Int, offset: Long): List<Example>

    /**
     * Try to insert an example into the storage.
     *
     * @param example An example to be inserted into the storage.
     * @return Inserted example.
     *
     * @throws NoSuchElementException If the card to which the example belongs is not found in the storage.
     */
    suspend fun insert(example: Example): Example

    /**
     * Try to update the example in the storage.
     *
     * @param example The example that must be updated.
     * @return Updated example.
     *
     * @throws IllegalArgumentException If the identifier of the passed example is null.
     * @throws NoSuchElementException If the parent card is not found in the storage.
     */
    suspend fun update(example: Example): Example

    /**
     * Delete all examples from the storage.
     */
    suspend fun deleteAll()

    /**
     * Delete the example from the storage by its [id].
     *
     * @param id The unique identifier of the example that must be deleted.
     */
    suspend fun deleteById(id: Long)

    /**
     * Delete all examples from the storage that belong to the card with an identifier equal to [id].
     *
     * @param id The unique identifier of the card to which examples belong.
     */
    suspend fun deleteByCardId(id: Long)
}