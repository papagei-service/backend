package com.yaroslavzghoba.utils

/**
 * Contains constants that, unlike the constants defined in the configuration file,
 * are available anywhere in the program.
 */
object Constants {

    /**
     * A claim key that specifies whether the token is strong.
     */
    const val STRONG_TOKEN_CLAIM_KEY = "strong"

    /**
     * A claim key that identifies the token owner.
     */
    const val OWNER_TOKEN_CLAIM_KEY = "owner"
}