package space.zghoba.routing.v1.account

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.model.LoginCredentials
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.hashing.HashingService
import space.zghoba.security.sessions.UserSession
import space.zghoba.utils.Constants

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.postLogin(
    repository: Repository,
    sessionStorage: SessionStorage,
    hashingService: HashingService,
): suspend RoutingContext.() -> Unit = postLoginHandler@{

    // Receive credentials sent by the client
    val loginCredentials = runCatching { call.receive<LoginCredentials>() }.getOrNull()
    if (loginCredentials == null) {
        val message = mapOf("message" to "The request body cannot be converted to a login credentials.")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postLoginHandler
    }

    // Return 404 if no user with the corresponding name is found in the user storage
    val correspondingUser = repository.getUserByUsername(username = loginCredentials.username)
    if (correspondingUser == null) {
        val message = mapOf("message" to "There is no the user with the \"${loginCredentials.username}\" username")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@postLoginHandler
    }

    // Return 401 if the password hash sent by the client does not match the hash of the corresponding user
    val inputPasswordHash = hashingService
        .hash(password = loginCredentials.password, salt = correspondingUser.salt)
    if (inputPasswordHash != correspondingUser.hashedPassword) {
        val message = mapOf("message" to "The password is incorrect")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postLoginHandler
    }

    // Delete the previous user session, if provided, and create a new one
    call.attributes.allKeys
        .firstOrNull { it.name == Constants.USER_SESSION_ID_ATTRIBUTE_KEY }
        ?.also { sessionStorage.invalidate(call.sessionId<UserSession>()!!) }
        ?.also { call.attributes.remove(it) }
    call.sessions.set(UserSession(userId = correspondingUser.id!!))

    val message = mapOf("message" to "Login was successful")
    call.respond(status = HttpStatusCode.OK, message = message)
}