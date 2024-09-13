package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.model.Credentials
import com.yaroslavzghoba.routing.RouteHandlersProvider
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private typealias PostSignUpHandler =
        suspend RoutingContext.(userStorage: UserStorage) -> Unit

val RouteHandlersProvider.Api.V1.postSignUp: PostSignUpHandler
    get() = postSignUp@{ userStorage ->
        val credentials = call.receive<Credentials>()

        // Return 401 if the user with the same username is already exists
        userStorage.getByUsername(username = credentials.username)?.let { user ->
            val message =
                mapOf("message" to "The user with the \"${user.username}\" username is already exists")
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = message,
            )
            return@postSignUp
        }

        // Return 401 if the username is blank
        if (credentials.username.isBlank()) {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = mapOf("message" to "The username cannot be blank")
            )
            return@postSignUp
        }

        // Return 401 if the password is blank
        if (credentials.password.isBlank()) {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = mapOf("message" to "The password cannot be blank")
            )
            return@postSignUp
        }

        // Create an account and save it to the storage
        userStorage.insert(credentials.toNewUser())
        call.respond(
            status = HttpStatusCode.OK,
            message = mapOf("message" to "The account was created successfully")
        )
    }