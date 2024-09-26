package com.yaroslavzghoba.routing.v1.users

import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Users.postLogin(
    repository: Repository,
    hashingService: HashingService,
): suspend RoutingContext.() -> Unit = postLoginHandler@{

    // Receive credentials sent by the client
    val inputCredentials = call.receive<InputCredentials>()

    // Return 401 if no user with the corresponding name is found in the user storage
    val correspondingUser = repository.getUserByUsername(username = inputCredentials.username)
    if (correspondingUser == null) {
        val message = mapOf("meesage" to "There is no the user with the \"${inputCredentials.username}\" username")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postLoginHandler
    }

    // Return 401 if the password hash sent by the client does not match the hash of the corresponding user
    val inputPasswordHash = hashingService
        .hash(password = inputCredentials.password, salt = correspondingUser.salt)
    if (inputPasswordHash != correspondingUser.hashedPassword) {
        val message = mapOf("message" to "The password is incorrect")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postLoginHandler
    }

    // Generate a session
    call.sessions.set(UserSession(username = correspondingUser.username))

    val message = mapOf("message" to "Login was successful")
    call.respond(status = HttpStatusCode.OK, message = message)
}