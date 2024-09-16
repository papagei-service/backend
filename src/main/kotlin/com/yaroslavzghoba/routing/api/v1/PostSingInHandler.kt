package com.yaroslavzghoba.routing.api.v1

import com.yaroslavzghoba.data.UserStorage
import com.yaroslavzghoba.model.InputCredentials
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.hashing.HashingService
import com.yaroslavzghoba.security.jwt.JwtTokenClaim
import com.yaroslavzghoba.security.jwt.JwtTokenConfig
import com.yaroslavzghoba.security.jwt.JwtTokenService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RouteHandlersProvider.Api.V1.postSignIn(
    userStorage: UserStorage,
    hashingService: HashingService,
    jwtTokenConfig: JwtTokenConfig,
    jwtTokenService: JwtTokenService,
): suspend RoutingContext.() -> Unit = postSignInHandler@{

    // Receive credentials sent by the client
    val inputCredentials = call.receive<InputCredentials>()

    // Return 401 if no user with the corresponding name is found in the user storage
    val correspondingUser = userStorage.getByUsername(username = inputCredentials.username)
    if (correspondingUser == null) {
        val message = mapOf("meesage" to "There is no the user with the \"${inputCredentials.username}\" username")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postSignInHandler
    }

    // Return 401 if the password hash sent by the client does not match the hash of the corresponding user
    val inputPasswordHash = hashingService
        .hash(password = inputCredentials.password, salt = correspondingUser.salt)
    if (inputPasswordHash != correspondingUser.hashedPassword) {
        val message = mapOf("message" to "The password is incorrect")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postSignInHandler
    }

    // Generate and return a JWT token
    val usernameClaim = JwtTokenClaim(name = "username", value = inputCredentials.username)
    val token = jwtTokenService.generate(
        config = jwtTokenConfig.copy(claims = jwtTokenConfig.claims + usernameClaim)
    )
    call.respond(mapOf("token" to token))
}