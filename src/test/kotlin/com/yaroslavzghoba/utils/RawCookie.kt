package com.yaroslavzghoba.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * Add the raw cookie value with the “Cookie” key to the http request headers.
 *
 * @receiver The builder of the request to which the cookie header will be added.
 * @param value The raw value of the “Cookie” header.
 */
@Suppress("unused", "nothing_to_inline")
inline fun HttpRequestBuilder.rawCookie(value: String) =
    header(key = "Cookie", value = value)

/**
 * Extract a value from the response's headers by the "Set-Cookie" key.
 *
 * @receiver The response to the http request from which the cookie value will be attempted.
 * @return A value of the cookie header.
 * @throws NoSuchHeaderException If a header with "Set-Cookie" key is not found.
 */
@Suppress("unused", "nothing_to_inline")
inline fun HttpResponse.rawCookie(): String =
    headers["Set-Cookie"] ?: throw NoSuchHeaderException()

/**
 * Generated if the http response header with the requested key is not found.
 */
class NoSuchHeaderException : NoSuchElementException()