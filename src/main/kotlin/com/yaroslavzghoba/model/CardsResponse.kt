package com.yaroslavzghoba.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response to a client that requested a list of cards.
 *
 * @param totalCount Total number of cards found by the client's query.
 * @param cards Cards that match the client's request.
 */
@Serializable
data class CardsResponse(
    @SerialName("total_count") val totalCount: Long,
    @SerialName("cards") val cards: List<Card>
)