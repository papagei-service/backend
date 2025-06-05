package com.yaroslavzghoba.utils

import com.yaroslavzghoba.model.InputCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object AuthUtils {
    const val STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6InRydWUiLCJpYXQiOjE3MjY4NTIzOTd9.WW2fj_gRrGD2I6BklSHIS03Q8hBUMUhHxX7jDIcKs-s"
    const val NOT_STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvYXBpIiwiaXNzIjoiL2FwaS9yZWdpc3RlciIsInN0cm9uZyI6ImZhbHNlIiwiaWF0IjoxNzI2ODUyMzk3fQ.8Vfa3gaj7nY0Ov5Om5nJFcEs4RbFLaREc_89Fi2wv4U"

    suspend fun registerUser(
        client: HttpClient,
        inputCredentials: InputCredentials,
        token: String,
    ) {
        client.post("/v1/account/register") {
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
        return client.post("/v1/account/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(inputCredentials)
        }
    }
}