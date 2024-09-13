package com.yaroslavzghoba.utils

/**
 * Logs the given [message] and the line separator in red color to the standard output stream.
 *
 * @param message The message that must be logged.
 */
@Suppress("unused")  // Mark the IDE that the function is actually used
fun logInRed(message: Any?) = println("\u001b[31m$message\u001b[0m")