package space.zghoba.utils

/**
 * Adds special codes to the string that tell the console that the message should be printed in red.
 *
 * @receiver Message to which special codes must be added.
 * @return Message with special codes.
 */
@Suppress("unused")
fun String.toRed() = "\u001b[31m$this\u001b[0m"