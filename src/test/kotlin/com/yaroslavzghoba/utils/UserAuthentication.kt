package com.yaroslavzghoba.utils

import com.yaroslavzghoba.model.InputCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object AuthUtils {
    suspend fun registerUser(
        client: HttpClient,
        inputCredentials: InputCredentials,
        token: String,
    ) {
        client.post("/v1/users/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }
    }

    suspend fun loginUser(
        client: HttpClient,
        inputCredentials: InputCredentials,
        token: String,
    ): HttpResponse {
        return client.post("/v1/users/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }
    }
}