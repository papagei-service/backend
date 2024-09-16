package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.SaltGenerator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RouteHandlersProvider.Api.V1.postSignUp(
    userStorage: UserStorage,
    hashingService: HashingService,
    saltGenerator: SaltGenerator,
): suspend RoutingContext.() -> Unit = postSignUpHandler@{

    // Receive credentials sent by the client
    val inputCredentials = call.receive<InputCredentials>()

    // Return 401 if the user with the same username is already exists
    val correspondingUser = userStorage.getByUsername(username = inputCredentials.username)
    correspondingUser?.let {
        val message = mapOf("message" to "The user with the \"${it.username}\" username is already exists")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postSignUpHandler
    }

    // Return 401 if the input username is blank
    if (inputCredentials.username.isBlank()) {
        val message = mapOf("message" to "The username cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postSignUpHandler
    }

    // Return 401 if the input password is blank
    if (inputCredentials.password.isBlank()) {
        val message = mapOf("message" to "The password cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postSignUpHandler
    }

    // Create an account and save it to the storage
    val user = User.Builder(inputCredentials = inputCredentials, hashingService = hashingService)
        .withSalt(salt = saltGenerator.generate())
        .build()
    userStorage.insert(user = user)

    val message = mapOf("message" to "The account was created successfully")
    call.respond(status = HttpStatusCode.OK, message = message)
}