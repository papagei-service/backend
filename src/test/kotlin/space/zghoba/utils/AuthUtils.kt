package space.zghoba.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import space.zghoba.model.LoginCredentials
import space.zghoba.model.RegistrationCredentials

object AuthUtils {
    const val STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvdjEiLCJpc3MiOiIvdjEvdG9rZW4iLCJzdHJvbmciOiJ0cnVlIiwiaWF0IjoxNzUwNTQwMTg0fQ.3TaS_csDP-nLM6obI84oUw6YzXq7pwB1umyx_ivc-3M"
    const val NOT_STRONG_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIvdjEiLCJpc3MiOiIvdjEvdG9rZW4iLCJzdHJvbmciOiJmYWxzZSIsImlhdCI6MTc1MDU0MDE4NH0.fwPG0GsqTVsiL4I9-zu8rC528mQf3tvVqQTXFgpN9Iw"

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