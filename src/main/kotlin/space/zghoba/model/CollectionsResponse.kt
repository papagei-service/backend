package space.zghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response to a client that requested a list of collections.
 *
 * @param totalCount Total number of collections found by the client's query.
 * @param collections Collections that match the client's request.
 */
@Serializable
data class CollectionsResponse(
    @SerialName("total_count") val totalCount: Long,
    @SerialName("collections") val collections: List<CardCollection>
)