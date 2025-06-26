package com.yaroslavzghoba.routing.v1.account

import com.yaroslavzghoba.model.RegistrationCredentials
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

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.postRegister(
    repository: Repository,
    hashingService: HashingService,
    saltConfig: PasswordSaltConfig,
    saltGenerator: KeyGenerator,
): suspend RoutingContext.() -> Unit = postRegisterHandler@{

    // Receive credentials sent by the client
    val registrationCredentials = runCatching { call.receive<RegistrationCredentials>() }.getOrNull()
    if (registrationCredentials == null) {
        val message = mapOf("message" to "The request body cannot be converted to a registration credentials.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the user with the same username is already exists
    val correspondingUser = repository.getUserByUsername(username = registrationCredentials.username)
    if (correspondingUser != null) {
        val message = mapOf("message" to "The username \"${correspondingUser.username}\" is already taken.")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the input username is blank
    if (registrationCredentials.username.isBlank()) {
        val message = mapOf("message" to "The username cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the input display name is blank
    if (registrationCredentials.displayName.isBlank()) {
        val message = mapOf("message" to "The display name cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Return 401 if the input password is blank
    if (registrationCredentials.password.isBlank()) {
        val message = mapOf("message" to "The password cannot be blank")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postRegisterHandler
    }

    // Create an account and save it to the storage
    val saltLength = with(saltConfig) { minLength..maxLength }.random()
    val salt = saltGenerator.generate(length = saltLength)
    val userToInsert = User.Builder(registrationCredentials = registrationCredentials, hashingService = hashingService)
        .withSalt(salt = salt)
        .build()
    repository.insertUser(user = userToInsert)

    val message = mapOf("message" to "The account was created successfully")
    call.respond(status = HttpStatusCode.Created, message = message)
}