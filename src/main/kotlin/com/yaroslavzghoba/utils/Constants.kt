package com.yaroslavzghoba.utils

/**
 * Contains constants that, unlike the constants defined in the configuration file,
 * are available anywhere in the program.
 */
object Constants {

    /**
     * A claim key that specifies whether the JWT token is strong.
     */
    const val STRONG_TOKEN_CLAIM_KEY = "strong"

    /**
     * A claim key that identifies the JWT token owner.
     */
    const val OWNER_TOKEN_CLAIM_KEY = "owner"

    /**
     * Name of the limit parameter.
     */
    const val LIMIT_PARAM_NAME = "limit"

    /**
     * Default maximum number of entities returned to the client.
     */
    const val DEFAULT_LIMIT = 20

    /**
     * Name of the offset parameter.
     */
    const val OFFSET_PARAM_NAME = "offset"

    /**
     * Default offset parameter for entities returned to the client.
     */
    const val DEFAULT_OFFSET: Long = 0

    /**
     * Name of the parameter that contains a list of card fields separated by commas for sorting.
     */
    const val SORT_BY_PARAM_NAME = "sort"

    /**
     * Default list of card fields separated by commas for sorting.
     * Use `-` for descending order and `+` for ascending order.
     * The order of fields in the list determines the sort priority.
     * Example: -show_next_time_at,+known_language_text
     */
    val DEFAULT_SORT_BY = null

    /**
     * Delimiter between different sortings.
     */
    const val SORTING_DELIMITER = ","

    /**
     * Name of a parameter that contains a time point is designed to select only cards
     * where the time of the next show is greater than or less than the passed value.
     *
     * A combination of time and date in ISO 8601 format is expected.
     * For example: "2007-04-05T14:30Z" or "2007-04-05T12:30-02:00".
     * To learn more, read: https://en.wikipedia.org/wiki/ISO_8601#Combined_date_and_time_representations
     */
    const val NEXT_TIME_BEFORE_PARAM_NAME = "next_time_before"

    /**
     * Default value of a parameter that contains a time point is designed to select only cards
     * where the time of the next show is greater than or less than the passed value.
     *
     * A combination of time and date in ISO 8601 format is expected.
     * For example: "2007-04-05T14:30Z" or "2007-04-05T12:30-02:00".
     * To learn more, read: https://en.wikipedia.org/wiki/ISO_8601#Combined_date_and_time_representations
     */
    val DEFAULT_NEXT_TIME_BEFORE: String? = null
}