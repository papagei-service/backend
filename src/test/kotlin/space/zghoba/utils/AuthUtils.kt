package space.zghoba.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import space.zghoba.model.LoginCredentials
import space.zghoba.model.RegistrationCredentials

object AuthUtils {
    const val STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvdjEiLCJpc3MiOiIvdjEvdG9rZW4iLCJzdHJvbmciOiJ0cnVlIiwiaWF0IjoxNzY2Nzk1NjkzfQ.ZDVzUQSanBgPeI3seYNkzdE-UsGPNpyxd2mWGg2dXk8"
    const val NOT_STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvdjEiLCJpc3MiOiIvdjEvdG9rZW4iLCJzdHJvbmciOiJmYWxzZSIsImlhdCI6MTc2Njc5NTY5M30.7TxgruzR3aCfbux1mzk35d7UhDe8ZY7ihNcKPvCex4M"

    suspend fun registerUser(
        client: HttpClient,
        registrationCredentials: RegistrationCredentials,
        token: String,
    ) {
        client.post("/v1/account/register") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(registrationCredentials)
        }
    }

    suspend fun loginUser(
        client: HttpClient,
        loginCredentials: LoginCredentials,
        token: String,
    ): HttpResponse {
        return client.post("/v1/account/login") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(loginCredentials)
        }
    }
}