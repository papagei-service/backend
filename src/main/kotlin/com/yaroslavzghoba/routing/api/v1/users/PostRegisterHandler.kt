package com.yaroslavzghoba.routing.api.v1.users

import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.model.User
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.hashing.PasswordSaltConfig
import com.yaroslavzghoba.utils.KeyGenerator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RouteHandlersProvider.Api.V1.Users.postRegister(
    repository: Repository,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    saltGenerator: KeyGenerator,
): suspend RoutingContext.() -> Unit = postRegisterHandler@{

    // Receive credentials sent by the client
    val inputCredentials = call.receive<InputCredentials>()

    // Return 401 if the user with the same username is already exists
    val correspondingUser = repository.getUserByUsername(username = inputCredentials.username)
    correspondingUser?.let {
        val message = mapOf("message" to "The user with the \"${it.username}\" username is already exists")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the input username is blank
    if (inputCredentials.username.isBlank()) {
        val message = mapOf("message" to "The username cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the input password is blank
    if (inputCredentials.password.isBlank()) {
        val message = mapOf("message" to "The password cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Create an account and save it to the storage
    val saltLength = with(saltConfig) { minLength..maxLength }.random()
    val salt = saltGenerator.generate(length = saltLength)
    val user = User.Builder(inputCredentials = inputCredentials, hashingService = hashingService)
        .withSalt(salt = salt)
        .build()
    repository.insertUser(user = user)

    val message = mapOf("message" to "The account was created successfully")
    call.respond(status = HttpStatusCode.OK, message = message)
}