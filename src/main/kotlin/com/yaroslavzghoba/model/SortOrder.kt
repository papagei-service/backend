package com.yaroslavzghoba.model

/**
 * Represents the sort order of a specific column in a SQL database table.
 *
 * @param parameterCode The name of the code that the client specifies in the request parameters in the URL.
 */
enum class SortOrder(val parameterCode: String) {

    /**
     * Sort in ascending order.
     */
    ASC(parameterCode = "asc"),

    /**
     * Sort in descending order.
     */
    DESC(parameterCode = "desc"),
}