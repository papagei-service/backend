package com.yaroslavzghoba.routing.v1.account

import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.deleteAccount(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteAccountHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 404 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session!!.userId)
    repository.deleteUserById(id = correspondingUser!!.id!!)
    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}