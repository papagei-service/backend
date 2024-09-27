package com.yaroslavzghoba.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of a request to insert a card.
 *
 * @param frontTitle A title placed on the front side of the card.
 * @param frontDescription A description placed on the front side of the card.
 * @param frontExample An example of usage placed on the front side of the card.
 * @param backTitle A title placed on the back side of the card.
 * @param backDescription A description placed on the back side of the card.
 * @param backExample An example placed on the back side of the card.
 * @param nextTimeAt UTC time when you need to show the card next time.
 * @param currentIntervalMs Current interval between card displays in milliseconds.
 */
@Serializable
data class PostCardRequest(
    @SerialName("front_title") val frontTitle: String,
    @SerialName("front_description") val frontDescription: String?,
    @SerialName("front_example") val frontExample: String?,
    @SerialName("back_title") val backTitle: String,
    @SerialName("back_description") val backDescription: String?,
    @SerialName("back_example") val backExample: String?,
    @SerialName("next_time_at") val nextTimeAt: Instant,
    @SerialName("current_interval_ms") val currentIntervalMs: Long,
)
