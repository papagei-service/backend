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
}