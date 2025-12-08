package space.zghoba.routing.v1.account

import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.postLogout(

): suspend RoutingContext.() -> Unit = postLogoutHandler@{
    call.sessions.clear<UserSession>()

    val message = mapOf("message" to "The user session was successfully deleted")
    call.respond(status = HttpStatusCode.OK, message = message)
}